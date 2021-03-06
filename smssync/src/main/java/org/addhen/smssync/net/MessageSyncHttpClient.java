/*
 * Copyright (c) 2010 - 2015 Ushahidi Inc
 * All rights reserved
 * Contact: team@ushahidi.com
 * Website: http://www.ushahidi.com
 * GNU Lesser General Public License Usage
 * This file may be used under the terms of the GNU Lesser
 * General Public License version 3 as published by the Free Software
 * Foundation and appearing in the file LICENSE.LGPL included in the
 * packaging of this file. Please review the following information to
 * ensure the GNU Lesser General Public License version 3 requirements
 * will be met: http://www.gnu.org/licenses/lgpl.html.
 *
 * If you have questions regarding the use of this file, please contact
 * Ushahidi developers at team@ushahidi.com.
 */
package org.addhen.smssync.net;

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.squareup.otto.Produce;

import org.addhen.smssync.R;
import org.addhen.smssync.models.Message;
import org.addhen.smssync.models.SmssyncResponse;
import org.addhen.smssync.models.SyncUrl;
import org.addhen.smssync.net.SyncScheme.SyncDataFormat;
import org.addhen.smssync.net.SyncScheme.SyncDataKey;
import org.addhen.smssync.net.SyncScheme.SyncMethod;
import org.addhen.smssync.util.DataFormatUtil;
import org.addhen.smssync.util.Util;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.protocol.HTTP;

import java.util.Locale;

public class MessageSyncHttpClient extends MainHttpClient {

    private SyncUrl syncUrl;

    private String serverError;

    private String clientError;

    private SmssyncResponse smssyncResponse;

    public MessageSyncHttpClient(
            Context context, SyncUrl syncUrl, Message message, String toNumber, String deviceId
    ) {
        super(syncUrl.getUrl(), context);
        this.syncUrl = syncUrl;
        initRequest(message, toNumber, deviceId);

    }

    private void initRequest(Message message, String toNumber, String deviceId) {

        SyncScheme syncScheme = syncUrl.getSyncScheme();
        SyncMethod method = syncScheme.getMethod();
        SyncDataFormat format = syncScheme.getDataFormat();

        setHeader("Content-Type", syncScheme.getContentType());
        addParam(syncScheme.getKey(SyncDataKey.SECRET), syncUrl.getSecret());
        addParam(syncScheme.getKey(SyncDataKey.FROM), message.getPhoneNumber());
        addParam(syncScheme.getKey(SyncDataKey.MESSAGE), message.getBody());
        if (message.getDate() != null) {
            addParam(
                    syncScheme.getKey(SyncDataKey.SENT_TIMESTAMP), String.valueOf(
                            message.getDate().getTime())
            );
        }
        addParam(syncScheme.getKey(SyncDataKey.SENT_TO), toNumber);
        addParam(syncScheme.getKey(SyncDataKey.MESSAGE_ID), message.getUuid());
        addParam(syncScheme.getKey(SyncDataKey.DEVICE_ID), deviceId);
        try {
            setHttpEntity(format);
        } catch (Exception e) {
            log("Failed to set request body", e);
            setClientError("Failed to format request body" + e.getLocalizedMessage());
        }

        try {
            switch (method) {
                case POST:
                    setMethod(HttpMethod.POST);
                    break;
                case PUT:
                    setMethod(HttpMethod.PUT);
                    break;
                default:
                    log("Invalid server method");
                    setClientError("Failed to set request method.");
            }
        } catch (Exception e) {
            log("failed to set request method.", e);
            setClientError("Failed to set request method.");
        }

    }

    /**
     * Post sms to the configured sync URL
     *
     * @return boolean
     */
    public boolean postSmsToWebService() {

        try {
            execute();
            String response = getResponse();
            int statusCode = responseCode();

            if (statusCode != 200 && statusCode != 201) {
                setServerError("bad http return code", statusCode);
                return false;
            }
            final Gson gson = new Gson();
            SmssyncResponse smssyncResponses = gson.fromJson(response, SmssyncResponse.class);
            if (smssyncResponses.getPayload().isSuccess()) {
                // auto response message is enabled to be received from the
                // server.
                setServerSuccessResp(smssyncResponses);
                return true;
            }

            String payloadError = smssyncResponses.getPayload().getError();
            if (!TextUtils.isEmpty(payloadError)) {
                setServerError(payloadError, statusCode);
            } else {
                setServerError(getResponseErrorMessage(), statusCode);
            }
        } catch (Exception e) {
            log("Request failed", e);
            setClientError("Request failed. " + e.getMessage());
        }

        return false;

    }

    /**
     * Get HTTP Entity populated with data in a format specified by the current sync scheme
     */
    private void setHttpEntity(SyncDataFormat format) throws Exception {
        switch (format) {
            case JSON:
                setStringEntity(DataFormatUtil.makeJSONString(getParams()));
                log("setHttpEntity format JSON");
                Util.logActivities(context, "setHttpEntity format JSON");
                break;
            case XML:
                //TODO: Make parent node URL specific as well
                setStringEntity(DataFormatUtil.makeXMLString(getParams(), "payload", HTTP.UTF_8));
                log("setHttpEntity format XML");
                Util.logActivities(context, context.getString(R.string.http_entity_format, "XML"));
                break;
            case YAML:
                setStringEntity(DataFormatUtil.makeYAMLString(getParams()));
                log("setHttpEntity format YAML");
                Util.logActivities(context, context.getString(R.string.http_entity_format, "YAML"));
                break;
            case URLEncoded:
                log("setHttpEntity format URLEncoded");
                Util.logActivities(context,
                        context.getString(R.string.http_entity_format, "URLEncoded"));
                setEntity(new UrlEncodedFormEntity(getParams(), HTTP.UTF_8));
                break;
            default:
                Util.logActivities(context, context.getString(R.string.invalid_data_format));
                throw new Exception("Invalid data format");
        }

    }

    public String getClientError() {
        return this.clientError;
    }

    public void setClientError(String error) {
        log("Client error " + error);
        Resources res = context.getResources();
        this.clientError = String.format(Locale.getDefault(), "%s",
                res.getString(R.string.sending_failed_custom_error, error));
        Util.logActivities(context, clientError);
    }

    public String getServerError() {
        return this.serverError;
    }

    public void setServerError(String error, int statusCode) {
        log("Server error " + error);
        Resources res = context.getResources();

        final String customError = res.getString(R.string.sending_failed_custom_error, error);

        final String code = res.getString(R.string.sending_failed_http_code, statusCode);

        this.serverError = String
                .format("%s %s ", customError, code);
        Util.logActivities(context, serverError);
    }

    public SmssyncResponse getServerSuccessResp() {
        return this.smssyncResponse;
    }

    public void setServerSuccessResp(SmssyncResponse smssyncResponse) {
        this.smssyncResponse = smssyncResponse;
    }

    @Produce
    public boolean reloadLog() {
        return true;
    }

    public SyncUrl getSyncUrl() {
        return syncUrl;
    }

}
