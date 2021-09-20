using System.Collections.Generic;
using System.Linq;
using DatabaseSchemaReader.DataSchema;
using static System.StringComparison;

namespace HttpServer.DbUtil
{
    public interface IDbInspector
    {
        DatabaseSchema GetSchema();

        bool IsFk(string table, string key) =>
            GetFk(table, key) != null;

        DatabaseConstraint GetFk(string table, string key) =>
            GetSchema().Tables
                .Single(x => x.Name.Equals(table, OrdinalIgnoreCase))
                .ForeignKeys
                .SingleOrDefault(fk => fk.Columns.Single().Equals(key, OrdinalIgnoreCase));

        string GetFkTarget(string table, string key) =>
            GetFk(table, key).RefersToTable;

        DatabaseColumn GetPkColumn(string table) =>
            GetSchema().FindTableByName(table).PrimaryKeyColumn;


        string GetId(string table, IDictionary<string, object> item) =>
            item[GetPkColumn(table).Name]?.ToString();

        
        
        
        string GetTitle(string tab, IDictionary<string, object> dictionary) =>
            dictionary["title"]?.ToString() ?? 
            dictionary["name"]?.ToString() ?? 
            dictionary["handle"]?.ToString() ?? 
            GetId(tab, dictionary);
    }
}