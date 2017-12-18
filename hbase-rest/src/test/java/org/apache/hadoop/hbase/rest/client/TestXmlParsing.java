begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements.  See the NOTICE file distributed with  * this work for additional information regarding copyright ownership.  * The ASF licenses this file to you under the Apache License, Version 2.0  * (the "License"); you may not use this file except in compliance with  * the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
operator|.
name|client
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
name|assertTrue
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
name|fail
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
name|xml
operator|.
name|bind
operator|.
name|UnmarshalException
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
name|rest
operator|.
name|Constants
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
name|model
operator|.
name|StorageClusterVersionModel
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
name|apache
operator|.
name|hadoop
operator|.
name|util
operator|.
name|StringUtils
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

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_comment
comment|/**  * Test class for {@link RemoteAdmin} to verify XML is parsed in a certain manner.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
name|SmallTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestXmlParsing
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|TestXmlParsing
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testParsingClusterVersion
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|String
name|xml
init|=
literal|"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
operator|+
literal|"<ClusterVersion Version=\"2.0.0\"/>"
decl_stmt|;
name|Client
name|client
init|=
name|mock
argument_list|(
name|Client
operator|.
name|class
argument_list|)
decl_stmt|;
name|RemoteAdmin
name|admin
init|=
operator|new
name|RemoteAdmin
argument_list|(
name|client
argument_list|,
name|HBaseConfiguration
operator|.
name|create
argument_list|()
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|Response
name|resp
init|=
operator|new
name|Response
argument_list|(
literal|200
argument_list|,
literal|null
argument_list|,
name|xml
operator|.
name|getBytes
argument_list|()
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|client
operator|.
name|get
argument_list|(
literal|"/version/cluster"
argument_list|,
name|Constants
operator|.
name|MIMETYPE_XML
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|resp
argument_list|)
expr_stmt|;
name|StorageClusterVersionModel
name|cv
init|=
name|admin
operator|.
name|getClusterVersion
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|"2.0.0"
argument_list|,
name|cv
operator|.
name|getVersion
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testFailOnExternalEntities
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|String
name|externalEntitiesXml
init|=
literal|"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
operator|+
literal|"<!DOCTYPE foo [<!ENTITY xxe SYSTEM \"/tmp/foo\"> ]>"
operator|+
literal|"<ClusterVersion>&xee;</ClusterVersion>"
decl_stmt|;
name|Client
name|client
init|=
name|mock
argument_list|(
name|Client
operator|.
name|class
argument_list|)
decl_stmt|;
name|RemoteAdmin
name|admin
init|=
operator|new
name|RemoteAdmin
argument_list|(
name|client
argument_list|,
name|HBaseConfiguration
operator|.
name|create
argument_list|()
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|Response
name|resp
init|=
operator|new
name|Response
argument_list|(
literal|200
argument_list|,
literal|null
argument_list|,
name|externalEntitiesXml
operator|.
name|getBytes
argument_list|()
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|client
operator|.
name|get
argument_list|(
literal|"/version/cluster"
argument_list|,
name|Constants
operator|.
name|MIMETYPE_XML
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|resp
argument_list|)
expr_stmt|;
try|try
block|{
name|admin
operator|.
name|getClusterVersion
argument_list|()
expr_stmt|;
name|fail
argument_list|(
literal|"Expected getClusterVersion() to throw an exception"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|assertEquals
argument_list|(
literal|"Cause of exception ought to be a failure to parse the stream due to our "
operator|+
literal|"invalid external entity. Make sure this isn't just a false positive due to "
operator|+
literal|"implementation. see HBASE-19020."
argument_list|,
name|UnmarshalException
operator|.
name|class
argument_list|,
name|e
operator|.
name|getCause
argument_list|()
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
specifier|final
name|String
name|exceptionText
init|=
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
decl_stmt|;
specifier|final
name|String
name|expectedText
init|=
literal|"\"xee\""
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"exception text: '"
operator|+
name|exceptionText
operator|+
literal|"'"
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Exception does not contain expected text"
argument_list|,
name|exceptionText
operator|.
name|contains
argument_list|(
name|expectedText
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

