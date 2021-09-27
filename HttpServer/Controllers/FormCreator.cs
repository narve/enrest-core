using System.Collections.Generic;
using System.Collections.Immutable;
using System.Linq;
using System.Net.Http;
using System.Threading.Tasks;
using Dapper;
using DatabaseSchemaReader.DataSchema;
using DV8.Html.Elements;
using DV8.Html.Support;
using DV8.Html.Utils;
using HttpServer.DbUtil;

// ReSharper disable CoVariantArrayConversion

namespace HttpServer.Controllers
{
    public class FormCreator
    {
        private readonly IDbInspector _dbInspector;
        private readonly IDbConnectionProvider _connectionProvider;
        private readonly ILinkManager _linkManager;

        public FormCreator(IDbInspector dbInspector, IDbConnectionProvider connectionProvider, ILinkManager linkManager)
        {
            _dbInspector = dbInspector;
            _connectionProvider = connectionProvider;
            _linkManager = linkManager;
        }

        public Form Pimp(string title, Form form)
        {
            form.Subs = new[]
            {
                new Fieldset()
                {
                    Subs = new Legend(title).ToArray().Concat(form.Subs).ToArray(),
                }
            };
            return form;
        }

        public async Task<IHtmlElement> GetCreateForm(string table)
        {
            var tableInfo = _dbInspector.GetSchema().FindTableByName(table);
            return Pimp( $"Create new '{table}'", new Form
            {
                ExAttributes = new Dictionary<string, string>()
                {
                    { "enctype", "multipart/form-data" }
                },
                rel = "Create",
                Action = _linkManager.LinkToCreateAction(table),
                Method = HttpMethod.Post,
                Clz = $"create {table}",
                Subs = 
                        (await GetInputFields(tableInfo))
                        .Concat(GetSubmit(tableInfo))
                        .ToArray()
            });
        }

        public async Task<Form> GetDeleteForm(string table, string id) => Pimp($"Delete '{table}' # {id}",
            new Form
            {
                Method = HttpMethod.Post,
                Clz = "delete " + table,
                Action = _linkManager.LinkToDeleteAction(table, id),
                Subs = Form.Submit("Delete").ToArray(),
            });

        public async Task<IHtmlElement> GetEditForm(string table, IDictionary<string, object> item)
        {
            var tableInfo = _dbInspector.GetSchema().FindTableByName(table);
            var id = _dbInspector.GetId(table, item);
            return Pimp($"Edit '{table}' # {id}", new Form
            {
                ExAttributes = new Dictionary<string, string>()
                {
                    { "enctype", "multipart/form-data" }
                },
                rel = "edit",
                Action = _linkManager.LinkToEditAction(table, id),
                Method = HttpMethod.Post,
                Clz = $"edit {table}",
                Subs = (await GetInputFields(tableInfo))
                            .Concat(GetSubmit(tableInfo))
                            .ToArray()
            });
        }

        private IEnumerable<IHtmlElement> GetSubmit(DatabaseTable tableInfo)
        {
            return new IHtmlElement[] { new Label(), Form.Submit($"Save {tableInfo.Name}") };
        }

        private async Task<IHtmlElement[]> GetInputFields(DatabaseTable tableInfo, IDictionary<string, object> dict = null)
        {
            var tasks = tableInfo.Columns
                .Where(ApplicableForMutation)
                .Select(col => GetFormField(col, dict).GetAwaiter().GetResult())
                .SelectMany(f => new IHtmlElement[]
                {
                    new Label()
                    {
                        For = f.Id,
                        Text = f.Name,
                    },
                    f,
                })
                // .Select(f => f.Labelize(f.Name))
                // .Select(f => new Div(f))
                .ToArray();
            return tasks;
        }

        private bool ApplicableForMutation(DatabaseColumn arg) =>
            !arg.IsComputed && !arg.IsAutoNumber;

        private async Task<IFormElement> GetFormField(DatabaseColumn col, IDictionary<string, object> dictionary)
        {
            if (col.IsForeignKey)
            {
                return await GetSelector(col);
            }

            if (_dbInspector.IsLob(col))
            {
                return new Input
                {
                    Id = col.Name,
                    Name = col.Name,
                    InputType = "file",
                };
            }

            var value = dictionary?[col.Name]?.ToString();
            return Input.ForString(col.Name, value);
        }

        private async Task<IFormElement> GetSelector(DatabaseColumn col)
        {
            var otherTable = col.ForeignKeyTable;
            var rows = await GetSelectorRows(otherTable);
            return new Select
            {
                Id = col.Name,
                Name = col.Name,
                Subs = rows.Select(r => new Option(r.Key, r.Value)).ToArray(),
            };
        }

        private async Task<ImmutableSortedDictionary<string, string>> GetSelectorRows(DatabaseTable otherTable)
        {
            var select = $"select * from {otherTable}";
            var rows = await _connectionProvider.Get().QueryAsync(select);
            var items = rows.Cast<IDictionary<string, object>>().ToList()
                .Select(x => KeyValuePair.Create(_dbInspector.GetId(otherTable.Name, x), _dbInspector.GetTitle(otherTable.Name, x)))
                .ToImmutableSortedDictionary();
            return items;
        }
    }
}