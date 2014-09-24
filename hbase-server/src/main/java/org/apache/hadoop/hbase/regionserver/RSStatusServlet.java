begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

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
name|regionserver
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|servlet
operator|.
name|ServletException
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
name|HttpServlet
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
name|HttpServletRequest
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
name|HttpServletResponse
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
name|classification
operator|.
name|InterfaceAudience
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
name|tmpl
operator|.
name|regionserver
operator|.
name|RSStatusTmpl
import|;
end_import

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|RSStatusServlet
extends|extends
name|HttpServlet
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
annotation|@
name|Override
specifier|protected
name|void
name|doGet
parameter_list|(
name|HttpServletRequest
name|req
parameter_list|,
name|HttpServletResponse
name|resp
parameter_list|)
throws|throws
name|ServletException
throws|,
name|IOException
block|{
name|HRegionServer
name|hrs
init|=
operator|(
name|HRegionServer
operator|)
name|getServletContext
argument_list|()
operator|.
name|getAttribute
argument_list|(
name|HRegionServer
operator|.
name|REGIONSERVER
argument_list|)
decl_stmt|;
assert|assert
name|hrs
operator|!=
literal|null
operator|:
literal|"No RS in context!"
assert|;
name|resp
operator|.
name|setContentType
argument_list|(
literal|"text/html"
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|hrs
operator|.
name|isOnline
argument_list|()
condition|)
block|{
name|resp
operator|.
name|getWriter
argument_list|()
operator|.
name|write
argument_list|(
literal|"The RegionServer is initializing!"
argument_list|)
expr_stmt|;
name|resp
operator|.
name|getWriter
argument_list|()
operator|.
name|close
argument_list|()
expr_stmt|;
return|return;
block|}
name|RSStatusTmpl
name|tmpl
init|=
operator|new
name|RSStatusTmpl
argument_list|()
decl_stmt|;
if|if
condition|(
name|req
operator|.
name|getParameter
argument_list|(
literal|"format"
argument_list|)
operator|!=
literal|null
condition|)
name|tmpl
operator|.
name|setFormat
argument_list|(
name|req
operator|.
name|getParameter
argument_list|(
literal|"format"
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|req
operator|.
name|getParameter
argument_list|(
literal|"filter"
argument_list|)
operator|!=
literal|null
condition|)
name|tmpl
operator|.
name|setFilter
argument_list|(
name|req
operator|.
name|getParameter
argument_list|(
literal|"filter"
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|req
operator|.
name|getParameter
argument_list|(
literal|"bcn"
argument_list|)
operator|!=
literal|null
condition|)
name|tmpl
operator|.
name|setBcn
argument_list|(
name|req
operator|.
name|getParameter
argument_list|(
literal|"bcn"
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|req
operator|.
name|getParameter
argument_list|(
literal|"bcv"
argument_list|)
operator|!=
literal|null
condition|)
name|tmpl
operator|.
name|setBcv
argument_list|(
name|req
operator|.
name|getParameter
argument_list|(
literal|"bcv"
argument_list|)
argument_list|)
expr_stmt|;
name|tmpl
operator|.
name|render
argument_list|(
name|resp
operator|.
name|getWriter
argument_list|()
argument_list|,
name|hrs
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

