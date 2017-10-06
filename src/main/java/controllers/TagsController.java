package controllers;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Context;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;

import api.ReceiptResponse;
import io.dropwizard.jersey.sessions.Session;

import generated.tables.records.ReceiptsRecord;

import dao.ReceiptDao;

import java.util.List;

import static java.util.stream.Collectors.toList;

// For a Java class to be eligible to receive ANY requests
// it must be annotated with at least @Path
@Path("/tags")
@Produces(MediaType.APPLICATION_JSON)
public class TagsController {

    private final ReceiptDao receiptDao;

    public TagsController(ReceiptDao receiptDao) {
        this.receiptDao = receiptDao;
    }

    @PUT
    @Path("/{tag}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void toggleTag(@PathParam("tag") String tagName, int receiptId) {
        List<ReceiptsRecord> receiptRecords = receiptDao.checkTag(tagName, receiptId);

        if (!receiptRecords.isEmpty()) {
            receiptDao.putTag("", receiptId);
        } else {
            receiptDao.putTag(tagName, receiptId);
        }
    }

    @GET
    @Path("/{tag}")
    public List<ReceiptResponse> getRecordsWithTag(@PathParam("tag") String tagName) {
        List<ReceiptsRecord> receiptRecords = receiptDao.getReceiptsWithTag(tagName);
        return receiptRecords.stream().map(ReceiptResponse::new).collect(toList());
    }

}