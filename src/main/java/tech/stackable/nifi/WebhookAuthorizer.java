package tech.stackable.nifi;

import com.google.gson.Gson;
import org.apache.nifi.authorization.*;
import org.apache.nifi.authorization.exception.AuthorizationAccessException;
import org.apache.nifi.authorization.exception.AuthorizerCreationException;
import org.apache.nifi.authorization.exception.AuthorizerDestructionException;
import org.apache.nifi.components.PropertyValue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class WebhookAuthorizer implements Authorizer
{
    private final Gson gson = new Gson();

    private String webhookUrl;

    @Override
    public AuthorizationResult authorize(AuthorizationRequest authorizationRequest) throws AuthorizationAccessException {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(webhookUrl).openConnection();

            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");

            String data = gson.toJson(authorizationRequest);
            OutputStream os = conn.getOutputStream();
            os.write(data.getBytes());
            os.flush();

            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
            Map test = gson.fromJson(br.readLine(), Map.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return AuthorizationResult.approved();


    }

    @Override
    public void initialize(AuthorizerInitializationContext authorizerInitializationContext) throws AuthorizerCreationException {

    }

    @Override
    public void onConfigured(AuthorizerConfigurationContext authorizerConfigurationContext) throws AuthorizerCreationException {
        final PropertyValue authorizationEndpointUrl = authorizerConfigurationContext.getProperty("Authorizaten Endpoint URL");
        if (!authorizationEndpointUrl.isSet()) {
            throw new AuthorizerCreationException("'Authorizaten Endpoint URL' needs to be set!");
        }
        webhookUrl = authorizationEndpointUrl.getValue();
    }

    @Override
    public void preDestruction() throws AuthorizerDestructionException {

    }
}
