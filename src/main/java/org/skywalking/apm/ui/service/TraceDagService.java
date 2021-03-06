package org.skywalking.apm.ui.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.skywalking.apm.ui.creator.ImageCache;
import org.skywalking.apm.ui.creator.UrlCreator;
import org.skywalking.apm.ui.tools.HttpClientTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author pengys5
 */
@Service
public class TraceDagService {

    private Logger logger = LogManager.getFormatterLogger(TraceDagService.class);

    private Gson gson = new Gson();

    @Autowired
    private ImageCache imageCache;

    @Autowired
    private UrlCreator UrlCreator;

    public JsonObject buildGraphData(String timeBucketType, long startTime, long endTime) throws IOException {
        return loadDataFromServer(timeBucketType, startTime, endTime);
    }

    public JsonObject loadDataFromServer(String timeBucketType, long startTime, long endTime) throws IOException {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("timeBucketType", timeBucketType));
        params.add(new BasicNameValuePair("startTime", String.valueOf(startTime)));
        params.add(new BasicNameValuePair("endTime", String.valueOf(endTime)));

        String traceDagUrl = UrlCreator.compound("traceDag");
        String traceDagResponse = HttpClientTools.INSTANCE.get(traceDagUrl, params);
        logger.debug("trace dag response: %s", traceDagResponse);

        JsonObject dagJsonObj = gson.fromJson(traceDagResponse, JsonObject.class).getAsJsonObject();
        JsonArray nodesArray = dagJsonObj.get("nodes").getAsJsonArray();

        JsonArray newNodesArray = new JsonArray();
        for (int i = 0; i < nodesArray.size(); i++) {
            JsonObject nodeJsonObj = nodesArray.get(i).getAsJsonObject();
            Integer id = nodeJsonObj.get("id").getAsInt();
            String peer = nodeJsonObj.get("peer").getAsString();

            if (nodeJsonObj.has("component") && !nodeJsonObj.get("component").isJsonNull()) {
                String component = nodeJsonObj.get("component").getAsString();
                nodeJsonObj = createNodeGraph(id, peer, imageCache.getImage(component), component);
            } else {
                nodeJsonObj = createNodeGraph(id, peer, imageCache.getImage(ImageCache.UNDEFINED_IMAGE), "");
            }

            newNodesArray.add(nodeJsonObj);
        }
        dagJsonObj.add("nodes", newNodesArray);

        return dagJsonObj;
    }

    private JsonObject createNodeGraph(int id, String label, String image, String component) {
        boolean real = isRealNode(label);

        JsonObject nodeJsonObj = new JsonObject();
        nodeJsonObj.addProperty("id", id);

        if (real) {
            nodeJsonObj.addProperty("label", label);
        } else {
            nodeJsonObj.addProperty("label", component);
            nodeJsonObj.addProperty("title", label);
        }
        nodeJsonObj.addProperty("image", image);
        nodeJsonObj.addProperty("real", real);
        return nodeJsonObj;
    }

    private Boolean isRealNode(String label) {
        if (label.startsWith("[") && label.endsWith("]")) {
            return false;
        } else {
            return true;
        }
    }
}
