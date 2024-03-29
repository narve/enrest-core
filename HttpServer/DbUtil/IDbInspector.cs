﻿using System.Collections.Generic;
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

        string GetFkTarget(string table, string key)
        {
            var fk = GetFk(table, key);
            if (fk.RefersToTable == null)
            {
                var constraint = fk.RefersToConstraint;
                if (constraint == "users_pkey")
                    return "users";
            } 
            return fk.RefersToTable;
        }

        DatabaseColumn GetPkColumn(string table) =>
            GetSchema().FindTableByName(table)?.PrimaryKeyColumn;


        string GetId(string table, IDictionary<string, object> item) =>
            item[GetPkColumn(table)?.Name ?? "id"].ToString();


        string GetTitle(string tab, IDictionary<string, object> dictionary) =>
            dictionary["title"]?.ToString() ??
            dictionary["name"]?.ToString() ??
            dictionary["handle"]?.ToString() ??
            dictionary["email"]?.ToString() ??
            GetTitle(tab, GetId(tab, dictionary));

        string GetTitle(string tab, string id) => tab + "#" + id;

        bool IsLob(DatabaseColumn col) => col.DbDataType == "bytea";

        bool IsLob(string table, string kvpKey)
        {
            var c = GetColumn(table, kvpKey);
            return c != null && IsLob(c);
        }

        DatabaseColumn GetColumn(string table, string kvpKey) => GetSchema().FindTableByName(table).FindColumn(kvpKey);
    }
}