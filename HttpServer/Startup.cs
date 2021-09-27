using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net;
using System.Text.Json.Serialization;
using System.Threading.Tasks;
using HttpServer.Controllers;
using HttpServer.DbUtil;
using HttpServer.Middleware;
using Microsoft.AspNetCore.Builder;
using Microsoft.AspNetCore.Hosting;
using Microsoft.AspNetCore.HttpsPolicy;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Authentication;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.AspNetCore.Diagnostics;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc.Formatters;
using Microsoft.AspNetCore.Mvc.Infrastructure;
using Microsoft.AspNetCore.Mvc.ModelBinding;
using Microsoft.Identity.Web;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Hosting;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Options;
using Microsoft.OpenApi.Models;

namespace HttpServer
{
    public class Startup
    {
        public Startup(IConfiguration configuration)
        {
            Configuration = configuration;
        }

        public IConfiguration Configuration { get; }

        public static FormatterCollection<IOutputFormatter> Outputs; 
        
        // This method gets called by the runtime. Use this method to add services to the container.
        public void ConfigureServices(IServiceCollection services)
        {
            // services.AddAuthentication(JwtBearerDefaults.AuthenticationScheme)
            // .AddMicrosoftIdentityWebApi(Configuration.GetSection("AzureAdB2C"));

            // services.AddSingleton<ILinkMangler, GuidMangler>();
            
            services.AddSingleton<ILinkMangler, NoMangler>();
            
            services.AddMvc(options =>
            {
                options.OutputFormatters.Insert(0, new HtmlOutputFormatter());
                options.RespectBrowserAcceptHeader = true; // false by default
                Outputs = options.OutputFormatters;
            }).AddJsonOptions(opts =>
            {
                opts.JsonSerializerOptions.IgnoreNullValues = true;
                // opts.JsonSerializerOptions.Converters.Add(new JsonLinkConverter());
                // opts.JsonSerializerOptions.Converters.Add(new JsonLinksConverter());
            });
            

            services.AddHttpContextAccessor();
            // services.AddSingleton<IDbInspector>(new DbInspector(new DbConnectionProvider(Configuration)));
            services.AddSingleton<IDbInspector, DbInspector>();
            services.AddSingleton<FormCreator>();
            services.AddSingleton<DbMutator>();
            services.AddSingleton<ILinkManager, LinkManager>();
            services.AddSingleton<IDbConnectionProvider, DbConnectionProvider>();

            services.AddControllers();
            services.AddTransient<ProblemDetailsFactory, CustomProblemDetailsFactory>();            
            services.AddSwaggerGen(c => { c.SwaggerDoc("v1", new OpenApiInfo { Title = "HttpServer", Version = "v1" }); });
            
            services.AddHostedService<DbInspectorLoader>();
        }

        // This method gets called by the runtime. Use this method to configure the HTTP request pipeline.
        public void Configure(IApplicationBuilder app, IWebHostEnvironment env)
        {
            if (env.IsDevelopment())
            {
                // app.UseDeveloperExceptionPage();
                // app.UseSwagger();
                // app.UseSwaggerUI(c => c.SwaggerEndpoint("/swagger/v1/swagger.json", "HttpServer v1"));
            }

            // app.UseExceptionHandler(HandleException);
            app.UseMiddleware<ExceptionMiddleware>();

            app.UseHttpsRedirection();

            app.UseMiddleware<LinkManglerMiddleware>();

            app.UseDefaultFiles();
            app.UseStaticFiles();

            app.UseRouting();

            // app.UseAuthentication();
            // app.UseAuthorization();

            app.UseEndpoints(endpoints => { endpoints.MapControllers(); });
        }

        private void HandleException(IApplicationBuilder errorApp)
        {
            errorApp.Run(async context =>
            {
                var exceptionHandlerPathFeature = context.Features.Get<IExceptionHandlerPathFeature>();
                var ex = exceptionHandlerPathFeature?.Error;
                // if (ex is ValidationProblemDetails)
                // {
                    // var x = context.RequestServices.GetRequiredService<HtmlOutputFormatter>();
                    // return;
                // }

                ProblemDetailsFactory pf; 

                context.Response.StatusCode = (int)HttpStatusCode.InternalServerError;
                context.Response.ContentType = "text/html";

                await context.Response.WriteAsync("<html lang=\"en\"><body>\r\n");
                await context.Response.WriteAsync("ERROR!<br><br>\r\n");


                if (exceptionHandlerPathFeature?.Error is FileNotFoundException)
                {
                    await context.Response.WriteAsync(
                        "File error thrown!<br><br>\r\n");
                }

                await context.Response.WriteAsync(
                    "<a href=\"/\">Home</a><br>\r\n");
                await context.Response.WriteAsync("</body></html>\r\n");
                await context.Response.WriteAsync(new string(' ', 512));
            });
        }
    }

    public class CustomProblemDetailsFactory: ProblemDetailsFactory
    {
        public override ProblemDetails CreateProblemDetails(HttpContext httpContext, int? statusCode = null, string title = null, string type = null, string detail = null,
            string instance = null)
        {
            throw new NotImplementedException();
        }

        public override ValidationProblemDetails CreateValidationProblemDetails(HttpContext httpContext, ModelStateDictionary modelStateDictionary, int? statusCode = null,
            string title = null, string type = null, string detail = null, string instance = null)
        {
            throw new NotImplementedException();
        }
    }
}