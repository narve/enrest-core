using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace HttpServer.Controllers
{
    public class FavIconController
    {
        [HttpGet("favicon.ico")]
        [AllowAnonymous]
        public ActionResult GetFavIcon()
        {
            const string svg = @"
<svg
  xmlns=""http://www.w3.org/2000/svg""
  viewBox=""0 0 16 16"">

  <text x=""0"" y=""14"">🦄</text>
</svg>
";
            return new ContentResult { Content = svg };
        }

        
    }
}