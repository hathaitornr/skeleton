package controllers;

import api.ReceiptSuggestionResponse;
import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import org.hibernate.validator.constraints.NotEmpty;

import static java.lang.System.out;

@Path("/images")
@Consumes(MediaType.TEXT_PLAIN)
@Produces(MediaType.APPLICATION_JSON)
public class ReceiptImageController {
    private final AnnotateImageRequest.Builder requestBuilder;

    public ReceiptImageController() {
        // DOCUMENT_TEXT_DETECTION is not the best or only OCR method available
        Feature ocrFeature = Feature.newBuilder().setType(Feature.Type.TEXT_DETECTION).build();
        this.requestBuilder = AnnotateImageRequest.newBuilder().addFeatures(ocrFeature);

    }

    /**
     * This borrows heavily from the Google Vision API Docs.  See:
     * https://cloud.google.com/vision/docs/detecting-fulltext
     *
     * YOU SHOULD MODIFY THIS METHOD TO RETURN A ReceiptSuggestionResponse:
     *
     * public class ReceiptSuggestionResponse {
     *     String merchantName;
     *     String amount;
     * }
     */
    @POST
    public ReceiptSuggestionResponse parseReceipt(@NotEmpty String base64EncodedImage) throws Exception {
        Image img = Image.newBuilder().setContent(ByteString.copyFrom(Base64.getDecoder().decode(base64EncodedImage))).build();
        AnnotateImageRequest request = this.requestBuilder.setImage(img).build();

        try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
            BatchAnnotateImagesResponse responses = client.batchAnnotateImages(Collections.singletonList(request));
            AnnotateImageResponse res = responses.getResponses(0);

            if (res.getTextAnnotationsList() == null || res.getTextAnnotationsList().isEmpty()) {
                return new ReceiptSuggestionResponse(null, null);
            }

            String merchantName = null;
            BigDecimal amount = null;

            final List<StringData> potentialMerchantNameList = new ArrayList<>();
            final List<NumericData> potentialAmountList = new ArrayList<>();

            for (EntityAnnotation annotation : res.getTextAnnotationsList()) {
                final BoundingPoly position = annotation.getBoundingPoly();
                final List<Vertex> vertices = position.getVerticesList();
                sortVertexList(vertices);

                out.printf("Sorted vertices : %s\n", annotation.getBoundingPoly());
                out.printf("Text: %s\n", annotation.getDescription());

                String data = annotation.getDescription();

                if (!isAggregateData(data)) {
                    if (isDouble(data)) {
                        potentialAmountList.add(new NumericData(Double.valueOf(data), vertices.get(0)));
                    } else {
                        potentialMerchantNameList.add(new StringData(data, vertices.get(0)));
                    }
                }
            }

            Collections.sort(potentialMerchantNameList, new Comparator<StringData>() {
                @Override
                public int compare(StringData o1, StringData o2) {
                    return compareVertices(o1.getTopLeftPosition(), o2.getTopLeftPosition());
                }
            });

            Collections.sort(potentialAmountList, new Comparator<NumericData>() {
                @Override
                public int compare(NumericData o1, NumericData o2) {
                    return compareVertices(o2.getTopLeftPosition(), o1.getTopLeftPosition());
                }
            });

            out.printf("Potential merchant names (sorted): %s\n", potentialMerchantNameList);
            out.printf("Potential amounts (sorted): %s\n", potentialAmountList);

            if (!potentialMerchantNameList.isEmpty()) {
                merchantName = potentialMerchantNameList.get(0).getData();
            }

            if (!potentialAmountList.isEmpty()) {
                amount = BigDecimal.valueOf(potentialAmountList.get(0).getData());
            }

            //TextAnnotation fullTextAnnotation = res.getFullTextAnnotation();
            return new ReceiptSuggestionResponse(merchantName, amount);
        }
    }

    private void sortVertexList(final List<Vertex> vertices) {
        vertices.stream().sorted((first, second) -> compareVertices(first, second)).collect(Collectors.toList());
    }

    private int compareVertices(final Vertex first, final Vertex second) {
        if (first.getY() == second.getY()) {
            return Integer.compare(first.getX(), second.getX());
        }
        return Integer.compare(first.getY(), second.getY());
    }

    private boolean isDouble(final String input) {
        try {
            Double.valueOf(input);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isAggregateData(final String input) {
        return Arrays.stream(new String[] {" ", "\n"}).parallel().anyMatch(input::contains);
    }

    /**
     * Data object containing the string data from Google vision API.
     */
    private class StringData {

        final String data;

        final Vertex topLeftPosition;

        public StringData(final String data, final Vertex topLeftPosition) {
            this.data = data;
            this.topLeftPosition = topLeftPosition;
        }

        public String getData() {
            return data;
        }

        public Vertex getTopLeftPosition() {
            return topLeftPosition;
        }

        public String toString() {
            return "String data: " + data + " top left: " + topLeftPosition;
        }

    }

    /**
     * Data object containing the string data from Google vision API.
     */
    private class NumericData {

        final Double data;

        final Vertex topLeftPosition;

        public NumericData(final Double data, final Vertex topLeftPosition) {
            this.data = data;
            this.topLeftPosition = topLeftPosition;
        }

        public Double getData() {
            return data;
        }

        public Vertex getTopLeftPosition() {
            return topLeftPosition;
        }

        public String toString() {
            return "Double data: " + data + " top left: " + topLeftPosition;
        }

    }

}
