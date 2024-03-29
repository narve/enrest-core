﻿using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net.Http;
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
            return new RedirectResult(_linkManager.LinkToItem(table, _dbInspector.GetId(table, inserted)), false, false);
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
                new A(_linkManager.LinkToItem(table, id), _dbInspector.GetTitle(table, o), "self")
                {
                    Itemscope = true,
                },
                new A(_linkManager.LinkToEditForm(table, id), $"Form for editing '{table}'#{id}", "create form")
                {
                    Itemscope = true,
                },
                new A(_linkManager.LinkToDeleteForm(table, id), "Form for deleting " + _dbInspector.GetTitle(table, o), "delete")
                {
                    Itemscope = true,
                },
            };

            var actions = table == "users"
                ? new A("/forms/login?user=" + o["email"]).ToArray()
                : Array.Empty<IHtmlElement>(); 

            var links = editLinks.Concat(outgoingFkLinks).Concat(incomingFkLinks).Concat(actions)
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
                .Select(c => KeyValuePair.Create(c, (object)_dbInspector.GetId(sourceTable, o)))
                .ToList();
            var name = $"Search '{fk.TableName}' where {filters.Select(kvp => kvp.Key + "=" + kvp.Value).JoinToString()}";
            var rel = fk.TableName;
            var url = _linkManager.LinkToQuery(fk.TableName, filters);
            return new A(url, name, rel)
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
                var id = filters.Single().Value;
                var title = _dbInspector.GetTitle(fk.RefersToTable, id.ToString());
                return new A(_linkManager.LinkToItem(fk.RefersToTable, id), title, fk.RefersToTable)
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


            var tabInfo = _dbInspector.GetSchema().FindTableByName(table);
            var tab = tabInfo.SchemaOwner.Equals("auth") ? "auth."+table : table;
            var sql = $"select * from {tab}{where}";
            var conn = await _dbConnectionProvider.Get();
            var rs = await conn.QueryAsync(sql, parameters);
            var rs2 = rs
                .Cast<IDictionary<string, object>>()
                .Select(d => AddSelfLink(table, d))
                // .Cast<IDictionary<string, object>>()
                .ToArray();
            foreach (var o in rs2)
            {
                o["type"] = table;
            }

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

        [HttpGet(ILinkManager.GetDeleteForm)]
        public async Task<IHtmlElement> GetDeleteForm(string table, string id)
        {
            // var dict = await _dbMutator.GetById(table, id);
            return await _formsCreator.GetDeleteForm(table, id);
        }


        [HttpPost(ILinkManager.DeleteAction)]
        public async Task<RedirectResult> DeleteAction(string table, string id)
        {
            var del = await _dbMutator.DeleteRow(table, id);
            if (del != 1)
                throw new Exception();
            return new RedirectResult(_linkManager.LinkToQuery(table));
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
                rel = t.Name,
            };

        [HttpGet]
        public object GetRoot() =>
            _dbInspector.GetSchema().Tables
                .Where(t => !"information_schema".Equals(t.SchemaOwner, StringComparison.OrdinalIgnoreCase))
                .Where(t => !"auth".Equals(t.SchemaOwner, StringComparison.OrdinalIgnoreCase))
                .Where(t => "public".Equals(t.SchemaOwner, StringComparison.OrdinalIgnoreCase))
                .Select(Link)
                .Concat(SystemLinks());

        [HttpGet("reload-db")]
        public object RefreshDatabaseInformation()
        {
            (_dbInspector as DbInspector).ReloadSchema();
            ;
            return "Done. ";
        }

        private IEnumerable<IHtmlElement> SystemLinks() =>
            new IHtmlElement[]
            {
                new A("/reload-db", "Refresh database information"),
                new A(ILinkManager.GetLogoutForm, "Logout"),
            };


        [HttpGet(ILinkManager.GetLoginForm)]
        public IHtmlElement GetLoginForm([FromQuery] string user)
        {
            return new Form
            {
                Action = ILinkManager.LoginAction,
                Method = HttpMethod.Post,
                Subs = new Fieldset
                {
                    Subs = new IHtmlElement[]
                    {
                        Input.ForString("user", user),
                        Form.Submit("Login"),
                    }
                }.ToArray()
            };
        }

        [HttpGet(ILinkManager.GetLogoutForm)]
        public IHtmlElement GetLogoutForm([FromQuery] string user)
        {
            return new Form
            {
                Action = ILinkManager.LogoutAction,
                Method = HttpMethod.Post,
                Subs = new Fieldset
                {
                    Subs = new IHtmlElement[]
                    {
                        Form.Submit("Logout"),
                    }
                }.ToArray()
            };
        }

        [HttpPost(ILinkManager.LoginAction)]
        public async Task<object> DoLogin([FromForm] string user)
        {
            var conn = await _dbConnectionProvider.Get();
            var rows = await conn.QueryAsync(
                "select id from auth.users where email = @email", 
                // "select id from auth.users  ", 
                new { email = user });
            dynamic uid = rows.First();
            var dict = (IDictionary<string, object>)uid;
            var uidString = dict["id"]?.ToString();
            if (string.IsNullOrEmpty(uidString))
                throw new ArgumentException($"Login failed for '{user}'");
            _httpContextAccessor.HttpContext.Response.Cookies.Append("username", user + "::" + uidString);
            // _httpContextAccessor.HttpContext.Response.Cookies.Append("username", user);
            return $"Logged in as {user} / {uid}";
        }
        
        [HttpPost(ILinkManager.LogoutAction)]
        public async Task<object> DoLogout()
        {
            var user = _httpContextAccessor.HttpContext.Request.Cookies["username"];
            _httpContextAccessor.HttpContext.Response.Cookies.Delete("username");
            return $"Logged out out {user}";
        }
    }
}