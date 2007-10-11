begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|generated
operator|.
name|master
package|;
end_package

begin_import
import|import
name|javax
operator|.
name|servlet
operator|.
name|*
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|servlet
operator|.
name|http
operator|.
name|*
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|servlet
operator|.
name|jsp
operator|.
name|*
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|*
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|HBaseConfiguration
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|shell
operator|.
name|TableFormatter
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|shell
operator|.
name|ReturnMsg
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|shell
operator|.
name|generated
operator|.
name|Parser
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|shell
operator|.
name|Command
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|shell
operator|.
name|formatter
operator|.
name|HtmlTableFormatter
import|;
end_import

begin_class
specifier|public
specifier|final
class|class
name|hql_jsp
extends|extends
name|org
operator|.
name|apache
operator|.
name|jasper
operator|.
name|runtime
operator|.
name|HttpJspBase
implements|implements
name|org
operator|.
name|apache
operator|.
name|jasper
operator|.
name|runtime
operator|.
name|JspSourceDependent
block|{
specifier|private
specifier|static
name|java
operator|.
name|util
operator|.
name|Vector
name|_jspx_dependants
decl_stmt|;
specifier|public
name|java
operator|.
name|util
operator|.
name|List
name|getDependants
parameter_list|()
block|{
return|return
name|_jspx_dependants
return|;
block|}
specifier|public
name|void
name|_jspService
parameter_list|(
name|HttpServletRequest
name|request
parameter_list|,
name|HttpServletResponse
name|response
parameter_list|)
throws|throws
name|java
operator|.
name|io
operator|.
name|IOException
throws|,
name|ServletException
block|{
name|JspFactory
name|_jspxFactory
init|=
literal|null
decl_stmt|;
name|PageContext
name|pageContext
init|=
literal|null
decl_stmt|;
name|HttpSession
name|session
init|=
literal|null
decl_stmt|;
name|ServletContext
name|application
init|=
literal|null
decl_stmt|;
name|ServletConfig
name|config
init|=
literal|null
decl_stmt|;
name|JspWriter
name|out
init|=
literal|null
decl_stmt|;
name|Object
name|page
init|=
name|this
decl_stmt|;
name|JspWriter
name|_jspx_out
init|=
literal|null
decl_stmt|;
name|PageContext
name|_jspx_page_context
init|=
literal|null
decl_stmt|;
try|try
block|{
name|_jspxFactory
operator|=
name|JspFactory
operator|.
name|getDefaultFactory
argument_list|()
expr_stmt|;
name|response
operator|.
name|setContentType
argument_list|(
literal|"text/html;charset=UTF-8"
argument_list|)
expr_stmt|;
name|pageContext
operator|=
name|_jspxFactory
operator|.
name|getPageContext
argument_list|(
name|this
argument_list|,
name|request
argument_list|,
name|response
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|,
literal|8192
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|_jspx_page_context
operator|=
name|pageContext
expr_stmt|;
name|application
operator|=
name|pageContext
operator|.
name|getServletContext
argument_list|()
expr_stmt|;
name|config
operator|=
name|pageContext
operator|.
name|getServletConfig
argument_list|()
expr_stmt|;
name|session
operator|=
name|pageContext
operator|.
name|getSession
argument_list|()
expr_stmt|;
name|out
operator|=
name|pageContext
operator|.
name|getOut
argument_list|()
expr_stmt|;
name|_jspx_out
operator|=
name|out
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
literal|"<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \n  \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\"> \n<html xmlns=\"http://www.w3.org/1999/xhtml\">\n<head><meta http-equiv=\"Content-Type\" content=\"text/html;charset=UTF-8\"/>\n<title>HQL</title>\n<link rel=\"stylesheet\" type=\"text/css\" href=\"/static/hbase.css\" />\n</head>\n\n<body>\n<a id=\"logo\" href=\"http://wiki.apache.org/lucene-hadoop/Hbase\"><img src=\"/static/hbase_logo_med.gif\" alt=\"Hbase Logo\" title=\"Hbase Logo\" /></a>\n<h1 id=\"page_title\"><a href=\"http://wiki.apache.org/lucene-hadoop/Hbase/HbaseShell\">HQL</a></h1>\n<p id=\"links_menu\"><a href=\"/master.jsp\">Home</a></p>\n<hr id=\"head_rule\" />\n"
argument_list|)
expr_stmt|;
name|String
name|query
init|=
name|request
operator|.
name|getParameter
argument_list|(
literal|"q"
argument_list|)
decl_stmt|;
if|if
condition|(
name|query
operator|==
literal|null
condition|)
block|{
name|query
operator|=
literal|""
expr_stmt|;
block|}
name|out
operator|.
name|write
argument_list|(
literal|"\n<form action=\"/hql.jsp\" method=\"get\">\n<p>\n<label for=\"query\">Query:</label>\n<input type=\"text\" name=\"q\" id=\"q\" size=\"40\" value=\""
argument_list|)
expr_stmt|;
name|out
operator|.
name|print
argument_list|(
name|query
argument_list|)
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
literal|"\" />\n<input type=\"submit\" value=\"submit\" />\n</p>\n</form>\n<p>Enter 'help;' -- thats 'help' plus a semi-colon -- for a list of<em>HQL</em> commands.\n Data Definition, SHELL, INSERTS, DELETES, and UPDATE commands are disabled in this interface\n</p>\n \n "
argument_list|)
expr_stmt|;
if|if
condition|(
name|query
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
name|out
operator|.
name|write
argument_list|(
literal|"\n<hr/>\n "
argument_list|)
expr_stmt|;
name|Parser
name|parser
init|=
operator|new
name|Parser
argument_list|(
name|query
argument_list|,
name|out
argument_list|,
operator|new
name|HtmlTableFormatter
argument_list|(
name|out
argument_list|)
argument_list|)
decl_stmt|;
name|Command
name|cmd
init|=
name|parser
operator|.
name|terminatedCommand
argument_list|()
decl_stmt|;
if|if
condition|(
name|cmd
operator|.
name|getCommandType
argument_list|()
operator|!=
name|Command
operator|.
name|CommandType
operator|.
name|SELECT
condition|)
block|{
name|out
operator|.
name|write
argument_list|(
literal|"\n<p>"
argument_list|)
expr_stmt|;
name|out
operator|.
name|print
argument_list|(
name|cmd
operator|.
name|getCommandType
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
literal|"-type commands are disabled in this interface.</p>\n "
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|ReturnMsg
name|rm
init|=
name|cmd
operator|.
name|execute
argument_list|(
operator|new
name|HBaseConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|summary
init|=
name|rm
operator|==
literal|null
condition|?
literal|""
else|:
name|rm
operator|.
name|toString
argument_list|()
decl_stmt|;
name|out
operator|.
name|write
argument_list|(
literal|"\n<p>"
argument_list|)
expr_stmt|;
name|out
operator|.
name|print
argument_list|(
name|summary
argument_list|)
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
literal|"</p>\n "
argument_list|)
expr_stmt|;
block|}
block|}
name|out
operator|.
name|write
argument_list|(
literal|"\n</body>\n</html>\n"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
if|if
condition|(
operator|!
operator|(
name|t
operator|instanceof
name|SkipPageException
operator|)
condition|)
block|{
name|out
operator|=
name|_jspx_out
expr_stmt|;
if|if
condition|(
name|out
operator|!=
literal|null
operator|&&
name|out
operator|.
name|getBufferSize
argument_list|()
operator|!=
literal|0
condition|)
name|out
operator|.
name|clearBuffer
argument_list|()
expr_stmt|;
if|if
condition|(
name|_jspx_page_context
operator|!=
literal|null
condition|)
name|_jspx_page_context
operator|.
name|handlePageException
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
if|if
condition|(
name|_jspxFactory
operator|!=
literal|null
condition|)
name|_jspxFactory
operator|.
name|releasePageContext
argument_list|(
name|_jspx_page_context
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

