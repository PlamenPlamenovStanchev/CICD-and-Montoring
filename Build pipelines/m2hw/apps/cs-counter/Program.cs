using Microsoft.AspNetCore.Builder;

var builder = WebApplication.CreateBuilder(args);
var app = builder.Build();

int count = 0;

app.MapGet("/", () => 
{
    count++;
    return $"Hello! This C# app has been viewed {count} times.\n";
});

app.Run("http://0.0.0.0:5000");
