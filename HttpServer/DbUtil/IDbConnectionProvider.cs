using System;
using System.Data;

namespace HttpServer.DbUtil
{
    public interface IDbConnectionProvider: IDisposable
    {
        IDbConnection Get(); 
    }
}