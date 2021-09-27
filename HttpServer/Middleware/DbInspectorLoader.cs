using System;
using System.Threading;
using System.Threading.Tasks;
using HttpServer.DbUtil;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Hosting;
using Microsoft.Extensions.Logging;

namespace HttpServer.Middleware
{
    public class DbInspectorLoader : IHostedService
    {
        private readonly IServiceProvider _serviceProvider;
        private readonly ILogger<DbInspectorLoader> _logger;

        public DbInspectorLoader(IServiceProvider serviceProvider, ILogger<DbInspectorLoader> logger)
        {
            _serviceProvider = serviceProvider;
            _logger = logger;
        }

        public Task StartAsync(CancellationToken cancellationToken)
        {
            _logger.LogInformation("Loading db schema ");
            using var scope = _serviceProvider.CreateScope();
            var svc = scope.ServiceProvider.GetRequiredService<IDbInspector>();
            var schema = svc.GetSchema();
            _logger.LogInformation("Schema was loaded - " + schema.Tables.Count + " tables found");
            return Task.CompletedTask;
        }

        public Task StopAsync(CancellationToken cancellationToken) => Task.CompletedTask;
    }
}