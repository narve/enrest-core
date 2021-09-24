using System;
using Microsoft.AspNetCore.Mvc;

namespace HttpServer.Middleware
{
    public class ProblemDetailsException: Exception
    {
        public ProblemDetails ProblemDetails { get; }

        public ProblemDetailsException(ProblemDetails problemDetails) => ProblemDetails = problemDetails;
        
    }
}