using System.Collections.Generic;
using System.Collections.Immutable;
using System.Linq;
using System.Net.Http;
using System.Threading.Tasks;
using Dapper;
using DatabaseSchemaReader.DataSchema;
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

        public async Task<IHtmlElement> GetEditForm(string table, IDictionary<string, object> item)
        {
            var tableInfo = _dbInspector.GetSchema().FindTableByName(table);
            var id = _dbInspector.GetId(table, item);
            return new Form
            {
                rel = "edit",
                Action = "/" + table + "/" + id,
                Method = HttpMethod.Post,
                Clz = $"edit {table}",
                Subs = (await GetInputFields(tableInfo, item)).Concat(GetSubmit(tableInfo)).ToArray()
            };
        }

        private IEnumerable<IHtmlElement> GetSubmit(DatabaseTable tableInfo)
        {
            return new[] { Form.Submit($"Save {tableInfo.Name}") };
        }

        private async Task<IHtmlElement[]> GetInputFields(DatabaseTable tableInfo, IDictionary<string, object> dict = null)
        {
            return await Task.WhenAll(tableInfo.Columns
                .Where(ApplicableForMutation)
                .Select(async col => await GetFormForField(col, dict))
                .ToArray()
            );
        }

        private bool ApplicableForMutation(DatabaseColumn arg) =>
            !arg.IsComputed && !arg.IsAutoNumber;

        private async Task<IHtmlElement> GetFormForField(DatabaseColumn col, IDictionary<string, object> dictionary)
        {
            if (col.IsForeignKey)
            {
                return await GetSelector(col);
            }

            var value = dictionary?[col.Name]?.ToString();
            return Input.ForString(col.Name, value);
        }

        private async Task<IHtmlElement> GetSelector(DatabaseColumn col)
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
                .Select(x => KeyValuePair.Create(_dbInspector.GetId(otherTable.Name, x), _dbInspector.GetTitle(otherTable.Name, x)))
                .ToImmutableSortedDictionary();
            return items;
        }
    }
}