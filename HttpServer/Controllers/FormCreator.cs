using System;
using System.Collections.Generic;
using System.Collections.Immutable;
using System.Linq;
using System.Net.Http;
using System.Threading.Tasks;
using Dapper;
using DatabaseSchemaReader.DataSchema;
using DatabaseSchemaReader.Utilities;
using DV8.Html.Elements;
using HttpServer.DbUtil;

// ReSharper disable CoVariantArrayConversion

namespace HttpServer.Controllers
{
    public class FormCreator
    {
        private readonly IDbInspector _dbInspector;
        private readonly IDbConnectionProvider _connectionProvider;

        public FormCreator(IDbInspector dbInspector, IDbConnectionProvider connectionProvider)
        {
            _dbInspector = dbInspector;
            _connectionProvider = connectionProvider;
        }

        public async Task<IHtmlElement> GetCreateForm(string table)
        {
            var tableInfo = _dbInspector.GetSchema().FindTableByName(table);
            return new Form
            {
                rel = "Create",
                Action = "/" + table,
                Method = HttpMethod.Post,
                Clz = $"create {table}",
                Subs = (await GetInputFields(tableInfo)).Concat(GetSubmit(tableInfo)).ToArray()
            };
        }

        private IEnumerable<IHtmlElement> GetSubmit(DatabaseTable tableInfo)
        {
            return new[] { Form.Submit(tableInfo.Name) };
        }

        private async Task<IHtmlElement[]> GetInputFields(DatabaseTable tableInfo)
        {
            return await Task.WhenAll(tableInfo.Columns
                .Where(ApplicableForInsert)
                .Select(async x => await GetFormForField(tableInfo, x))
                .ToArray()
            );
        }

        private bool ApplicableForInsert(DatabaseColumn arg)
        {
            return !arg.IsComputed && !arg.IsAutoNumber;
        }

        private async Task<IHtmlElement> GetFormForField(DatabaseTable tableInfo, DatabaseColumn col)
        {
            if (col.IsForeignKey)
            {
                return await GetSelector(tableInfo, col);
            }

            return Input.ForString(col.Name);
        }

        private async Task<IHtmlElement> GetSelector(DatabaseTable tableInfo, DatabaseColumn col)
        {
            var otherTable = col.ForeignKeyTable;
            var rows = await GetSelectorRows(otherTable);
            return new Select
            {
                Name = col.Name,
                Subs = rows.Select(r => new Option(r.Key, r.Value)).ToArray(),
            };
        }

        private async Task<ImmutableSortedDictionary<string, string>> GetSelectorRows(DatabaseTable otherTable)
        {
            var select = $"select id, name from {otherTable}";
            var rows = await _connectionProvider.Get().QueryAsync(select);
            var items = rows.Cast<IDictionary<string, object>>()
                .Select(x => KeyValuePair.Create("" + x["id"], "" + (x["name"] ?? x["id"].ToString())))
                .ToImmutableSortedDictionary();
            return items;
        }
    }
}