begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|rest
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertEquals
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertNotNull
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|mock
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|never
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|verify
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|when
import|;
end_import

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
name|ServletOutputStream
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
name|HBaseClassTestRule
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
name|rest
operator|.
name|filter
operator|.
name|GZIPResponseStream
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
name|rest
operator|.
name|filter
operator|.
name|GZIPResponseWrapper
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
name|testclassification
operator|.
name|RestTests
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
name|testclassification
operator|.
name|SmallTests
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|ClassRule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|experimental
operator|.
name|categories
operator|.
name|Category
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|RestTests
operator|.
name|class
block|,
name|SmallTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestGZIPResponseWrapper
block|{
annotation|@
name|ClassRule
specifier|public
specifier|static
specifier|final
name|HBaseClassTestRule
name|CLASS_RULE
init|=
name|HBaseClassTestRule
operator|.
name|forClass
argument_list|(
name|TestGZIPResponseWrapper
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|HttpServletResponse
name|response
init|=
name|mock
argument_list|(
name|HttpServletResponse
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|GZIPResponseWrapper
name|wrapper
init|=
operator|new
name|GZIPResponseWrapper
argument_list|(
name|response
argument_list|)
decl_stmt|;
comment|/**    * wrapper should set all headers except "content-length"    */
annotation|@
name|Test
specifier|public
name|void
name|testHeader
parameter_list|()
throws|throws
name|IOException
block|{
name|wrapper
operator|.
name|setStatus
argument_list|(
literal|200
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|response
argument_list|)
operator|.
name|setStatus
argument_list|(
literal|200
argument_list|)
expr_stmt|;
name|wrapper
operator|.
name|addHeader
argument_list|(
literal|"header"
argument_list|,
literal|"header value"
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|response
argument_list|)
operator|.
name|addHeader
argument_list|(
literal|"header"
argument_list|,
literal|"header value"
argument_list|)
expr_stmt|;
name|wrapper
operator|.
name|addHeader
argument_list|(
literal|"content-length"
argument_list|,
literal|"header value2"
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|response
argument_list|,
name|never
argument_list|()
argument_list|)
operator|.
name|addHeader
argument_list|(
literal|"content-length"
argument_list|,
literal|"header value"
argument_list|)
expr_stmt|;
name|wrapper
operator|.
name|setIntHeader
argument_list|(
literal|"header"
argument_list|,
literal|5
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|response
argument_list|)
operator|.
name|setIntHeader
argument_list|(
literal|"header"
argument_list|,
literal|5
argument_list|)
expr_stmt|;
name|wrapper
operator|.
name|setIntHeader
argument_list|(
literal|"content-length"
argument_list|,
literal|4
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|response
argument_list|,
name|never
argument_list|()
argument_list|)
operator|.
name|setIntHeader
argument_list|(
literal|"content-length"
argument_list|,
literal|4
argument_list|)
expr_stmt|;
name|wrapper
operator|.
name|setHeader
argument_list|(
literal|"set-header"
argument_list|,
literal|"new value"
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|response
argument_list|)
operator|.
name|setHeader
argument_list|(
literal|"set-header"
argument_list|,
literal|"new value"
argument_list|)
expr_stmt|;
name|wrapper
operator|.
name|setHeader
argument_list|(
literal|"content-length"
argument_list|,
literal|"content length value"
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|response
argument_list|,
name|never
argument_list|()
argument_list|)
operator|.
name|setHeader
argument_list|(
literal|"content-length"
argument_list|,
literal|"content length value"
argument_list|)
expr_stmt|;
name|wrapper
operator|.
name|sendRedirect
argument_list|(
literal|"location"
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|response
argument_list|)
operator|.
name|sendRedirect
argument_list|(
literal|"location"
argument_list|)
expr_stmt|;
name|wrapper
operator|.
name|flushBuffer
argument_list|()
expr_stmt|;
name|verify
argument_list|(
name|response
argument_list|)
operator|.
name|flushBuffer
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testResetBuffer
parameter_list|()
throws|throws
name|IOException
block|{
name|when
argument_list|(
name|response
operator|.
name|isCommitted
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|ServletOutputStream
name|out
init|=
name|mock
argument_list|(
name|ServletOutputStream
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|response
operator|.
name|getOutputStream
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|ServletOutputStream
name|servletOutput
init|=
name|wrapper
operator|.
name|getOutputStream
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|GZIPResponseStream
operator|.
name|class
argument_list|,
name|servletOutput
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
name|wrapper
operator|.
name|resetBuffer
argument_list|()
expr_stmt|;
name|verify
argument_list|(
name|response
argument_list|)
operator|.
name|setHeader
argument_list|(
literal|"Content-Encoding"
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|response
operator|.
name|isCommitted
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|servletOutput
operator|=
name|wrapper
operator|.
name|getOutputStream
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
name|out
operator|.
name|getClass
argument_list|()
argument_list|,
name|servletOutput
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|wrapper
operator|.
name|getWriter
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testReset
parameter_list|()
throws|throws
name|IOException
block|{
name|when
argument_list|(
name|response
operator|.
name|isCommitted
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|ServletOutputStream
name|out
init|=
name|mock
argument_list|(
name|ServletOutputStream
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|response
operator|.
name|getOutputStream
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|ServletOutputStream
name|servletOutput
init|=
name|wrapper
operator|.
name|getOutputStream
argument_list|()
decl_stmt|;
name|verify
argument_list|(
name|response
argument_list|)
operator|.
name|addHeader
argument_list|(
literal|"Content-Encoding"
argument_list|,
literal|"gzip"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|GZIPResponseStream
operator|.
name|class
argument_list|,
name|servletOutput
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
name|wrapper
operator|.
name|reset
argument_list|()
expr_stmt|;
name|verify
argument_list|(
name|response
argument_list|)
operator|.
name|setHeader
argument_list|(
literal|"Content-Encoding"
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|response
operator|.
name|isCommitted
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|servletOutput
operator|=
name|wrapper
operator|.
name|getOutputStream
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
name|out
operator|.
name|getClass
argument_list|()
argument_list|,
name|servletOutput
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSendError
parameter_list|()
throws|throws
name|IOException
block|{
name|wrapper
operator|.
name|sendError
argument_list|(
literal|404
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|response
argument_list|)
operator|.
name|sendError
argument_list|(
literal|404
argument_list|)
expr_stmt|;
name|wrapper
operator|.
name|sendError
argument_list|(
literal|404
argument_list|,
literal|"error message"
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|response
argument_list|)
operator|.
name|sendError
argument_list|(
literal|404
argument_list|,
literal|"error message"
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

