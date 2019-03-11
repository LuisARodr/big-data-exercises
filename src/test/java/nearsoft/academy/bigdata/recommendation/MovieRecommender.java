package nearsoft.academy.bigdata.recommendation;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

public class MovieRecommender {

    private int numReviews;
    public int numUsers;
    public int numProducts;
    private UserBasedRecommender recommender;
    private final String DATASETPATH = "src/resources/dataset.csv";
    private HashMap<String, Long> usersMap = new HashMap<String, Long>();
    private HashMap<String, Long> itemsMap = new HashMap<String, Long>();
    private FileDataModel model;

    public static void main(String[] args) throws IOException, TasteException {


    }

    public MovieRecommender(String path){
        numProducts = 0;
        numReviews = 0;
        numReviews = 0;
        createDataset(path);
        try {
            System.out.println("Creating datamodel...");
            model = new FileDataModel(new File(DATASETPATH));
            System.out.println("Done");
            UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
            UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
            recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (TasteException e) {
            e.printStackTrace();
        }
    }

    public int getTotalReviews(){

        return numReviews;
    }

    public int getTotalProducts(){

        return itemsMap.size();
    }

    public int getTotalUsers(){

        return usersMap.size();
    }

    public List<String> getRecommendationsForUser(String user){
        List<RecommendedItem> recommendations = null;
        List<String> recommendationsList = new ArrayList<String>();
        try {
            System.out.println("Making recomendations...");
            System.out.println("User:--: " + user + "Map: " +  usersMap.get(user));
            recommendations = recommender.recommend( usersMap.get(user), 3);
            HashMap<Long, String> invMap = (HashMap<Long, String>) invert(itemsMap);
            for (RecommendedItem recommendation : recommendations)
            {
                System.out.println("Long: " + recommendation + " ID: " + recommendation.getItemID() + " Value: " + invMap.get(recommendation.getItemID()));
                recommendationsList.add(""+ invMap.get(recommendation.getItemID()));
            }


        } catch (TasteException e) {
            e.printStackTrace();
        }
        return recommendationsList;
    }

    private static <V, K> Map<V, K> invert(Map<K, V> map) {

        Map<V, K> inv = new HashMap<V, K>();

        for (Map.Entry<K, V> entry : map.entrySet())
            inv.put(entry.getValue(), entry.getKey());

        return inv;
    }

    public void createDataset (String filePath){
        try
        {
            System.out.println("Creating dataset on " + DATASETPATH);
            new File(DATASETPATH).createNewFile();
            FileWriter fw = new FileWriter(new File(DATASETPATH));
            InputStream fileStream = new FileInputStream(filePath);
            InputStream gzipStream = new GZIPInputStream(fileStream);
            Reader decoder = new InputStreamReader(gzipStream,"UTF-8");



            BufferedReader br = new BufferedReader(decoder);
            BufferedWriter bw = new BufferedWriter(fw);
            String line = br.readLine();
            String[] lineControl = new String[3];
            Long[] lineMapControl = new Long[2];

            boolean firstLine = true;
            while (line != null){

                if(line.contains("product/productId"))
                {
                    if(!firstLine){
                        bw.write("\n");
                    }else{
                        firstLine = false;
                    }
                    lineControl[1] = line.split(":")[1].substring(1);
                    if(!itemsMap.containsKey(lineControl[1])){
                        itemsMap.put(lineControl[1], (long)numProducts);
                        lineMapControl[1] = (long)numProducts;
                        numProducts++;
                    }else{
                        lineMapControl[1] = itemsMap.get(lineControl[1]);
                    }

                }
                else if(line.contains("review/userId")){
                    lineControl[0] = line.split(":")[1].substring(1);

                    if(!usersMap.containsKey(lineControl[0])){
                        usersMap.put(lineControl[0], (long)numUsers);
                        lineMapControl[0] = (long)numUsers;
                        numUsers++;
                        //System.out.println("Small test: " + lineControl[0] + " value?" + (numUsers -1) );
                        //if(lineControl[0].equals("A141HP4LYPWMSR")){
                        //    System.out.println("We got em!");
                         //   System.out.println("usermap get= "+ usersMap.get("A141HP4LYPWMSR"));
                        //}
                    }else{
                        lineMapControl[0] = usersMap.get(lineControl[0]);
                    }
                }
                else if(line.contains("review/sc")){
                    lineControl[2] = line.split(":")[1].substring(1);
                    bw.write(lineMapControl[0]+","+lineMapControl[1]+","+lineControl[2]);
                    numReviews++;
                }

                line = br.readLine();
            }
            bw.close();
            System.out.println("Done");

        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }


}
