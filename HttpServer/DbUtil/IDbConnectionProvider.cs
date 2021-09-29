using System;
using System.Data;
using System.Threading.Tasks;

namespace HttpServer.DbUtil
{
    public interface IDbConnectionProvider: IDisposable
    {
        Task<IDbConnection> Get(); 
    }
}