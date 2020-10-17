package com.trevorism.gcloud.webapi.controller

import com.stripe.Stripe
import com.stripe.model.checkout.Session
import com.stripe.param.checkout.SessionCreateParams
import com.trevorism.gcloud.webapi.model.MemoMessage
import com.trevorism.gcloud.webapi.model.TextSubmission
import com.trevorism.gcloud.webapi.service.DefaultMessageService
import com.trevorism.gcloud.webapi.service.MessageService
import com.trevorism.secure.PropertiesProvider
import io.swagger.annotations.Api

import javax.ws.rs.Consumes
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.core.MediaType
import java.util.logging.Logger

@Api("Text Operations")
@Path("/text")
class TextController {

    private static final Logger log = Logger.getLogger(TextController.class.name)
    public static final BigDecimal COST_IN_USD = 1.99
    public static final int PREVIEW_LENGTH = 10
    private PropertiesProvider propertiesProvider = new PropertiesProvider()
    private MessageService messageService = new DefaultMessageService()

    @Path("submission")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    Map submitText(TextSubmission textSubmission) {
        log.info(textSubmission.toString())
        Stripe.apiKey = propertiesProvider.getProperty("stripeApiKey")

        String text = textSubmission.getText()
        int length = text.length() < PREVIEW_LENGTH ? text.length() : PREVIEW_LENGTH
        String preview = text[0..length - 1] + "..."

        SessionCreateParams.LineItem.PriceData.ProductData productData = SessionCreateParams.LineItem.PriceData.ProductData.builder().setName(preview).build()
        SessionCreateParams.LineItem.PriceData priceData = SessionCreateParams.LineItem.PriceData.builder()
                .setCurrency("usd")
                .setUnitAmount((long) (COST_IN_USD * 100))
                .setProductData(productData)
                .build()
        SessionCreateParams.LineItem lineItem = SessionCreateParams.LineItem.builder()
                .setQuantity(1L)
                .setPriceData(priceData)
                .build()

        SessionCreateParams params = SessionCreateParams.builder()
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("https://memo.trevorism.com/paymentsuccess")
                .setCancelUrl("https://memo.trevorism.com/")
                .addLineItem(lineItem)
                .build()

        messageService.saveMessage(new MemoMessage(type: "text", content: text))
        Session session = Session.create(params)
        return [id: session.getId()]
    }
}
