using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Threading.Tasks;
using Dapper;
using DatabaseSchemaReader.DataSchema;
using DV8.Html.Elements;
using DV8.Html.Utils;
using HttpServer.DbUtil;
using HttpServer.Models;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.WebUtilities;
using Microsoft.Extensions.Logging;
using Microsoft.Net.Http.Headers;

// ReSharper disable PossibleNullReferenceException

// ReSharper disable UnusedMember.Global

namespace HttpServer.Controllers
{
    // [Authorize]
    [ApiController]
    [Route("/")]
    public class ApiController
    {
        private readonly ILogger<ApiController> _logger;
        private readonly IDbInspector _dbInspector;
        private readonly IDbConnectionProvider _dbConnectionProvider;
        private readonly FormCreator _formsCreator;
        private readonly IHttpContextAccessor _httpContextAccessor;
        private readonly DbMutator _dbMutator;
        private readonly ILinkManager _linkManager;

        public ApiController(ILogger<ApiController> logger, IDbInspector dbInspector, IDbConnectionProvider dbConnectionProvider,
            FormCreator formsCreator, IHttpContextAccessor httpContextAccessor, DbMutator dbMutator, ILinkManager linkManager)
        {
            _logger = logger;
            _dbInspector = dbInspector;
            _dbConnectionProvider = dbConnectionProvider;
            _formsCreator = formsCreator;
            _httpContextAccessor = httpContextAccessor;
            _dbMutator = dbMutator;
            _linkManager = linkManager;
        }

        [HttpPost(ILinkManager.PostNewItem)]
        [DisableFormValueModelBinding]
        public async Task<RedirectResult> CreateObject(string table)
        {
            var kvpa = await ExtractFormValues();
            _logger.LogInformation("Should insert into {table} values {values}", table, kvpa.JoinToString());
            var inserted = await _dbMutator.InsertRow(table, kvpa);
            _logger.LogInformation("Inserted into {table}: {values}", table, inserted.DictToString());
            return new RedirectResult("/" + table + "/" + _dbInspector.GetId(table, inserted), false, false);
        }


        [HttpPost(ILinkManager.PostExistingItem)]
        [DisableFormValueModelBinding]
        public async Task<RedirectResult> UpdateObject(string table, string id)
        {
            var kvpa = await ExtractFormValues();

            _logger.LogInformation("Should update {table}#{id} values {values}", table, id, kvpa.JoinToString());
            var upd = await _dbMutator.UpdateRow(table, id, kvpa);
            _logger.LogInformation("Updated {table}#{id}: {values}", table, id, upd.DictToString());
            return new RedirectResult($"/{table}/{id}", false, false);
        }

        private async Task<List<KeyValuePair<string, object>>> ExtractFormValues()
        {
            var request = _httpContextAccessor.HttpContext.Request;
            var boundary = MultipartRequestHelper.GetBoundary(MediaTypeHeaderValue.Parse(request.ContentType));
            MultipartReader reader = new MultipartReader(boundary, request.BodyReader.AsStream());
            var kvpa = new List<KeyValuePair<string, object>>();

            var next = await reader.ReadNextSectionAsync();
            while (next != null)
            {
                var disposition = next.ContentDisposition;
                var isString = !disposition.Contains("filename=");
                var name = disposition.Split("; ")[1].Split("=")[1].Replace("\"", "");

                if (isString)
                {
                    using var streamReader = new StreamReader(next.Body);
                    var str = await streamReader.ReadToEndAsync();
                    kvpa.Add(KeyValuePair.Create<string, object>(name, str));
                }
                else
                {
                    await using var memoryStream = new MemoryStream();
                    await next.Body.CopyToAsync(memoryStream);
                    var bytes = memoryStream.ToArray();
                    kvpa.Add(KeyValuePair.Create<string, object>(name, bytes));
                }

                next = await reader.ReadNextSectionAsync();
            }

            return kvpa;
        }

        [HttpGet(ILinkManager.GetItemById)]
        public async Task<IDictionary<string, object>> GetById(string table, string id)
        {
            var dict = await _dbMutator.GetById(table, id);
            dict["_links"] = GetLinksForItem(table, dict);
            return dict;
        }

        private IHtmlElement GetLinksForItem(string table, IDictionary<string, object> o)
        {
            var allFks = _dbInspector.GetSchema().Tables
                .SelectMany(t => t.ForeignKeys).ToList();
            
            var incomingFkLinks = allFks
                .Where(fk => fk.RefersToTable == table)
                .Select(fk => GetInFkLink(table, fk, o))
                .ToArray();

            var outgoingFkLinks = allFks
                .Where(c => c.TableName == table)
                .Select(fk => GetOutFkLink(table, fk, o))
                .ToArray();

            var id = _dbInspector.GetId(table, o);
            var editLinks = new[]
            {
                new A(_linkManager.LinkToEditForm(table, id), $"Form for editing '{table}'#{id}", "create form")
                {
                    Itemscope = true,
                },
                new A(_linkManager.LinkToItem(table, id), _dbInspector.GetTitle(table, o), "self")
                {
                    Itemscope = true,
                },
            };

            var links = incomingFkLinks.Concat(editLinks).Concat(outgoingFkLinks)
                .Select(l => new Li(l))
                .Cast<IHtmlElement>()
                .ToArray();
            return new Ul
            {
                Itemprop = "_links",
                Subs = links,
            };
        }

        private A GetInFkLink(string sourceTable, DatabaseConstraint fk, IDictionary<string, object> o)
        {
            var filters = fk.Columns
                .Select(c => KeyValuePair.Create(c, (object)_dbInspector.GetId(sourceTable, o)));
            return new A(_linkManager.LinkToQuery(fk.TableName, filters))
            {
                Itemscope = true,
            };
        }

        private A GetOutFkLink(string sourceTable, DatabaseConstraint fk, IDictionary<string, object> o)
        {
            var filters = fk.Columns
                .Select((t, i) => KeyValuePair.Create(fk.ReferencedColumns(_dbInspector.GetSchema()).ToList()[i], o[t]))
                .ToList();
            if (filters.Count == 1)
            {
                return new A(_linkManager.LinkToItem(fk.RefersToTable, filters.Single().Value))
                {
                    Itemscope = true,
                };
            }
            return new A(_linkManager.LinkToQuery(fk.RefersToTable, filters))
            {
                Itemscope = true,
            };
        }

        [HttpGet(ILinkManager.GetItemsOfType)]
        public async Task<object> GetResultForTable(string table)
        {
            IQueryCollection query = _httpContextAccessor.HttpContext.Request.Query;
            var where = "";
            if (query.Any())
            {
                where += " WHERE ";
            }

            var parameters = new SortedDictionary<string, object>();
            foreach (var (key, value) in query)
            {
                where += key + "=@" + key;
                var colSpec = _dbInspector.GetSchema().FindTableByName(table).FindColumn(key);
                parameters.Add(key, _dbMutator.Coerce(colSpec, value.SingleOrDefault()));
            }


            var sql = $"select * from {table}{where}";
            var rs = await _dbConnectionProvider.Get().QueryAsync(sql, parameters);
            var rs2 = rs
                .Cast<IDictionary<string, object>>()
                .Select(d => AddSelfLink(table, d))
                .Cast<object>()
                .ToArray();
            return new SearchResult { Items = rs2, Links = LinksForTable(table) };
        }

        private IDictionary<string, object> AddSelfLink(string table, IDictionary<string, object> d)
        {
            d["_links"] = new[]
            {
                new A(_linkManager.LinkToItem(table, _dbInspector.GetId(table, d)), "self", "self")
            };
            return d;
        }

        [HttpGet(ILinkManager.GetCreateForm)]
        public async Task<IHtmlElement> GetCreateForm(string table)
        {
            return await _formsCreator.GetCreateForm(table);
        }

        [HttpGet(ILinkManager.DownloadLob)]
        public async Task<FileContentResult> DownloadLob(string table, string id, string field)
        {
            var bytes = await _dbMutator.GetBytes(table, id, field);
            return new FileContentResult(bytes, "application/jpg")
            {
                FileDownloadName = "attachment.jpg",
            };
        }

        [HttpGet(ILinkManager.GetEditForm)]
        public async Task<IHtmlElement> GetEditForm(string table, string id)
        {
            var dict = await _dbMutator.GetById(table, id);
            return await _formsCreator.GetEditForm(table, dict);
        }


        private A[] LinksForTable(string table) =>
            new[]
            {
                new A(_linkManager.LinkToCreateForm(table), $"Form for adding a '{table}'", "create form")
                {
                    Itemscope = true,
                }
            };

        private A Link(DatabaseTable t) =>
            new()
            {
                Href = _linkManager.LinkToQuery(t.Name),
                Text = $"{t.Name.UppercaseFirst()} {t.Description}",
            };

        [HttpGet]
        public object GetRoot() =>
            _dbInspector.GetSchema().Tables
                .Where(t => !"information_schema".Equals(t.SchemaOwner, StringComparison.OrdinalIgnoreCase))
                .Where(t => !"auth".Equals(t.SchemaOwner, StringComparison.OrdinalIgnoreCase))
                .Where(t => "public".Equals(t.SchemaOwner, StringComparison.OrdinalIgnoreCase))
                .Select(Link);
    }
}