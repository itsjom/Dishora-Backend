using Microsoft.AspNetCore.Mvc;
using System.Text; // Required for Content encoding

namespace Dishora.Controllers
{
    // Controller for handling the payment redirects (now simplified for App Links)
    [Route("payment")] // Keep route /payment/...
    public class PaymentRedirectController : Controller
    {
        [HttpGet("success")]
        public IActionResult Success([FromQuery] long draftId) // Use long to match DB
        {
            // Simple HTML page - App Link should intercept before this loads fully
            // if the app is installed and verified.
            // No need for a button or deep link here.
            var html = $@"<!DOCTYPE html>
                <html>
                <head><title>Payment Success</title><meta name='viewport' content='width=device-width, initial-scale=1'>
                <style>body {{ background-color: #121212; color: white; font-family: Arial, sans-serif; text-align: center; margin-top: 20%; }} h2 {{ color: #4CAF50; }}</style>
                </head><body><h2>Payment Successful!</h2><p>You may now close this browser.</p>
                <p><small>(Order Reference: {draftId})</small></p></body></html>";
            return Content(html, "text/html", Encoding.UTF8);
        }

        [HttpGet("cancel")]
        public IActionResult Cancel([FromQuery] long? draftId) // draftId might be null on cancel
        {
            // Simple HTML page - App Link should intercept this too.
            var html = $@"<!DOCTYPE html>
            <html>
            <head><title>Payment Cancelled</title><meta name='viewport' content='width=device-width, initial-scale=1'>
            <style>body {{ background-color: #121212; color: white; font-family: Arial, sans-serif; text-align: center; margin-top: 20%; }} h2 {{ color: #FF5252; }}</style>
            </head><body><h2>Payment Cancelled</h2><p>Returning to the Dishora app...</p>
            {(draftId.HasValue ? $"<p><small>(Ref: {draftId.Value})</small></p>" : "")}</body></html>";
            return Content(html, "text/html", Encoding.UTF8);
        }
    }
}

