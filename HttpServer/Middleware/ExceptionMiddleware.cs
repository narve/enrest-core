using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Text.Json;
using System.Threading.Tasks;
using DV8.Html.Utils;
using HttpServer.DbUtil;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.Abstractions;
using Microsoft.AspNetCore.Mvc.Formatters;
using Microsoft.AspNetCore.Routing;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Logging;
using Microsoft.Net.Http.Headers;

namespace HttpServer.Middleware
{
    public class ExceptionMiddleware
    {
        private readonly RequestDelegate _next;
        private bool _fullLeakageMode;

        public ExceptionMiddleware(RequestDelegate next)
        {
            _next = next;
        }

        public async Task InvokeAsync(HttpContext context, IServiceProvider provider, ILogger<ExceptionMiddleware> logger,
            IConfiguration config)
        {
            try
            {
                await _next(context);

                if (context.Response.StatusCode >= 400)
                {
                    var db = provider.GetRequiredService<IDbConnectionProvider>();
                    // db.MarkForRollback(context.Request.GetDisplayUrl() + " => " + context.Response.StatusCode);
                }

                if (context.Response.StatusCode == 401)
                {
                    // The authorization middleware does not throw an exception but sets the response status code.  
                    var validationError = HasAuthorizationHeader(context.Request);
                    // throw new ProblemDetailsException(HttpStatusCode.Unauthorized, validationError ? "invalid_token" : "missing_token",
                    //     GetTitle(context, validationError));
                }
            }
            catch (Exception ex)
            {
                // var scopeContext = provider.GetRequiredService<ScopeContext>();
                // var db = provider.GetRequiredService<IDbConnectionProvider>();
                // db.MarkForRollback(ex);
                // if (IsUserError(ex))
                // {
                //     logger.LogInformation(ex, $"Argument error: {ex.GetType()} {ex.Message} UserId: {scopeContext.UserInfo?.User}");
                // }
                // else
                // {
                //     logger.LogError(ex, $"Unexpected server error: {ex.GetType()} {ex.Message} UserId: {scopeContext.UserInfo?.User}");
                // }


                await HandleExceptionAsync(context, ex);
            }
        }

        public async Task HandleExceptionAsync(HttpContext context, Exception exception)
        {
            var details = CreateProblemDetails(exception);

            var result = new ObjectResult(details)
            {
                StatusCode = (int?)HttpStatusCode.BadRequest,
            };
            HtmlOutputFormatter.HtmlMediaTypes.ForEach(t => result.ContentTypes.Add(t));
            Startup.Outputs.ForEach(t => result.Formatters.Add(t));
            result.ContentTypes.Add(new MediaTypeHeaderValue("application/problem+json"));
            
            RouteData routeData = context.GetRouteData();
            ActionDescriptor actionDescriptor = new ActionDescriptor();
            ActionContext actionContext = new ActionContext(context, routeData, actionDescriptor);
            await result.ExecuteResultAsync(actionContext);
        }

        private ProblemDetails CreateProblemDetails(Exception x)
        {
            // ValidationProblemDetailsWrapper
            if (x is ProblemDetailsException o)
            {
                return o.ProblemDetails;
            }

            var statusCode = HttpStatusCode.InternalServerError;
            if (x is ArgumentException)
            {
                statusCode = HttpStatusCode.BadRequest;
            }

            var inner = x.GetBaseException();

            return new ProblemDetails()
            {
                Detail = "asdf" + x.ToString(),
                Title = "title",
            };
        }

        private static bool HasAuthorizationHeader(HttpRequest request)
        {
            return request.Headers[nameof(HttpRequestHeader.Authorization)].FirstOrDefault()
                ?.StartsWith("Bearer") ?? false;
        }

        private static string GetTitle(HttpContext context, bool validationError)
        {
            var jwtSecurityError = context.Response.Headers["WWW-Authenticate"].FirstOrDefault();
            if (jwtSecurityError != null && !jwtSecurityError.Equals("Bearer"))
            {
                return jwtSecurityError;
            }

            return validationError ? "Authentication token is invalid" : "Authentication token is not provided.";
        }

        //     private static bool IsUserError(Exception ex) =>
        //         ex is ProblemDetailsException pe && pe.ProblemDetails.IsArgumentError()
        //         ||
        //         ex is ArgumentException;
        // }        
    }
}