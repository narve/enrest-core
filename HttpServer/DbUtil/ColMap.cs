namespace HttpServer.DbUtil
{
    public class ColMap
    {
        public readonly string Column;
        public readonly string Property;
        public readonly string Definition;
        public readonly string SqlReference;

        public ColMap(string property, string definition) : this(property, property, definition)
        {
        }

        public ColMap(string column, string property, string definition) : this(column, property, definition, $"@{property}")
        {
        }

        private ColMap(string column, string property, string definition, string sqlReference)
        {
            Column = column;
            Property = property;
            Definition = definition;
            SqlReference = sqlReference;
        }

        public static ColMap CurrentTimeStamp(string prop) => 
            new ColMap(prop, prop, "DATETIMEOFFSET NOT NULL", "CURRENT_TIMESTAMP");
    }
}