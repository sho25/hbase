begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayInputStream
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
name|JAXBContext
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
name|JAXBException
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
name|conf
operator|.
name|Configuration
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
name|HBaseTestingUtility
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
name|NamespaceDescriptor
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
name|client
operator|.
name|Admin
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
name|client
operator|.
name|Client
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
name|client
operator|.
name|Cluster
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
name|client
operator|.
name|Response
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
name|NamespacesModel
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
name|TestNamespacesModel
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
name|MediumTests
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
name|util
operator|.
name|Bytes
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
name|*
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|AfterClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|BeforeClass
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
name|MediumTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestNamespacesResource
block|{
specifier|private
specifier|static
name|String
name|NAMESPACE1
init|=
literal|"TestNamespacesInstanceResource1"
decl_stmt|;
specifier|private
specifier|static
name|String
name|NAMESPACE2
init|=
literal|"TestNamespacesInstanceResource2"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|HBaseRESTTestingUtility
name|REST_TEST_UTIL
init|=
operator|new
name|HBaseRESTTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|Client
name|client
decl_stmt|;
specifier|private
specifier|static
name|JAXBContext
name|context
decl_stmt|;
specifier|private
specifier|static
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|static
name|TestNamespacesModel
name|testNamespacesModel
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setUpBeforeClass
parameter_list|()
throws|throws
name|Exception
block|{
name|conf
operator|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|()
expr_stmt|;
name|REST_TEST_UTIL
operator|.
name|startServletContainer
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|client
operator|=
operator|new
name|Client
argument_list|(
operator|new
name|Cluster
argument_list|()
operator|.
name|add
argument_list|(
literal|"localhost"
argument_list|,
name|REST_TEST_UTIL
operator|.
name|getServletPort
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|testNamespacesModel
operator|=
operator|new
name|TestNamespacesModel
argument_list|()
expr_stmt|;
name|context
operator|=
name|JAXBContext
operator|.
name|newInstance
argument_list|(
name|NamespacesModel
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|tearDownAfterClass
parameter_list|()
throws|throws
name|Exception
block|{
name|REST_TEST_UTIL
operator|.
name|shutdownServletContainer
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
specifier|private
specifier|static
name|NamespacesModel
name|fromXML
parameter_list|(
name|byte
index|[]
name|content
parameter_list|)
throws|throws
name|JAXBException
block|{
return|return
operator|(
name|NamespacesModel
operator|)
name|context
operator|.
name|createUnmarshaller
argument_list|()
operator|.
name|unmarshal
argument_list|(
operator|new
name|ByteArrayInputStream
argument_list|(
name|content
argument_list|)
argument_list|)
return|;
block|}
specifier|private
name|boolean
name|doesNamespaceExist
parameter_list|(
name|Admin
name|admin
parameter_list|,
name|String
name|namespaceName
parameter_list|)
throws|throws
name|IOException
block|{
name|NamespaceDescriptor
index|[]
name|nd
init|=
name|admin
operator|.
name|listNamespaceDescriptors
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|nd
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|nd
index|[
name|i
index|]
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
name|namespaceName
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
specifier|private
name|void
name|createNamespaceViaAdmin
parameter_list|(
name|Admin
name|admin
parameter_list|,
name|String
name|name
parameter_list|)
throws|throws
name|IOException
block|{
name|NamespaceDescriptor
operator|.
name|Builder
name|builder
init|=
name|NamespaceDescriptor
operator|.
name|create
argument_list|(
name|name
argument_list|)
decl_stmt|;
name|NamespaceDescriptor
name|nsd
init|=
name|builder
operator|.
name|build
argument_list|()
decl_stmt|;
name|admin
operator|.
name|createNamespace
argument_list|(
name|nsd
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testNamespaceListXMLandJSON
parameter_list|()
throws|throws
name|IOException
throws|,
name|JAXBException
block|{
name|String
name|namespacePath
init|=
literal|"/namespaces/"
decl_stmt|;
name|NamespacesModel
name|model
decl_stmt|;
name|Response
name|response
decl_stmt|;
comment|// Check that namespace does not yet exist via non-REST call.
name|Admin
name|admin
init|=
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
decl_stmt|;
name|assertFalse
argument_list|(
name|doesNamespaceExist
argument_list|(
name|admin
argument_list|,
name|NAMESPACE1
argument_list|)
argument_list|)
expr_stmt|;
name|model
operator|=
name|testNamespacesModel
operator|.
name|buildTestModel
argument_list|()
expr_stmt|;
name|testNamespacesModel
operator|.
name|checkModel
argument_list|(
name|model
argument_list|)
expr_stmt|;
comment|// Check that REST GET finds only default namespaces via XML and JSON responses.
name|response
operator|=
name|client
operator|.
name|get
argument_list|(
name|namespacePath
argument_list|,
name|Constants
operator|.
name|MIMETYPE_XML
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|200
argument_list|,
name|response
operator|.
name|getCode
argument_list|()
argument_list|)
expr_stmt|;
name|model
operator|=
name|fromXML
argument_list|(
name|response
operator|.
name|getBody
argument_list|()
argument_list|)
expr_stmt|;
name|testNamespacesModel
operator|.
name|checkModel
argument_list|(
name|model
argument_list|,
literal|"hbase"
argument_list|,
literal|"default"
argument_list|)
expr_stmt|;
name|response
operator|=
name|client
operator|.
name|get
argument_list|(
name|namespacePath
argument_list|,
name|Constants
operator|.
name|MIMETYPE_JSON
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|200
argument_list|,
name|response
operator|.
name|getCode
argument_list|()
argument_list|)
expr_stmt|;
name|model
operator|=
name|testNamespacesModel
operator|.
name|fromJSON
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|response
operator|.
name|getBody
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|testNamespacesModel
operator|.
name|checkModel
argument_list|(
name|model
argument_list|,
literal|"hbase"
argument_list|,
literal|"default"
argument_list|)
expr_stmt|;
comment|// Create namespace and check that REST GET finds one additional namespace.
name|createNamespaceViaAdmin
argument_list|(
name|admin
argument_list|,
name|NAMESPACE1
argument_list|)
expr_stmt|;
name|response
operator|=
name|client
operator|.
name|get
argument_list|(
name|namespacePath
argument_list|,
name|Constants
operator|.
name|MIMETYPE_XML
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|200
argument_list|,
name|response
operator|.
name|getCode
argument_list|()
argument_list|)
expr_stmt|;
name|model
operator|=
name|fromXML
argument_list|(
name|response
operator|.
name|getBody
argument_list|()
argument_list|)
expr_stmt|;
name|testNamespacesModel
operator|.
name|checkModel
argument_list|(
name|model
argument_list|,
name|NAMESPACE1
argument_list|,
literal|"hbase"
argument_list|,
literal|"default"
argument_list|)
expr_stmt|;
name|response
operator|=
name|client
operator|.
name|get
argument_list|(
name|namespacePath
argument_list|,
name|Constants
operator|.
name|MIMETYPE_JSON
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|200
argument_list|,
name|response
operator|.
name|getCode
argument_list|()
argument_list|)
expr_stmt|;
name|model
operator|=
name|testNamespacesModel
operator|.
name|fromJSON
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|response
operator|.
name|getBody
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|testNamespacesModel
operator|.
name|checkModel
argument_list|(
name|model
argument_list|,
name|NAMESPACE1
argument_list|,
literal|"hbase"
argument_list|,
literal|"default"
argument_list|)
expr_stmt|;
comment|// Create another namespace and check that REST GET finds one additional namespace.
name|createNamespaceViaAdmin
argument_list|(
name|admin
argument_list|,
name|NAMESPACE2
argument_list|)
expr_stmt|;
name|response
operator|=
name|client
operator|.
name|get
argument_list|(
name|namespacePath
argument_list|,
name|Constants
operator|.
name|MIMETYPE_XML
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|200
argument_list|,
name|response
operator|.
name|getCode
argument_list|()
argument_list|)
expr_stmt|;
name|model
operator|=
name|fromXML
argument_list|(
name|response
operator|.
name|getBody
argument_list|()
argument_list|)
expr_stmt|;
name|testNamespacesModel
operator|.
name|checkModel
argument_list|(
name|model
argument_list|,
name|NAMESPACE1
argument_list|,
name|NAMESPACE2
argument_list|,
literal|"hbase"
argument_list|,
literal|"default"
argument_list|)
expr_stmt|;
name|response
operator|=
name|client
operator|.
name|get
argument_list|(
name|namespacePath
argument_list|,
name|Constants
operator|.
name|MIMETYPE_JSON
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|200
argument_list|,
name|response
operator|.
name|getCode
argument_list|()
argument_list|)
expr_stmt|;
name|model
operator|=
name|testNamespacesModel
operator|.
name|fromJSON
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|response
operator|.
name|getBody
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|testNamespacesModel
operator|.
name|checkModel
argument_list|(
name|model
argument_list|,
name|NAMESPACE1
argument_list|,
name|NAMESPACE2
argument_list|,
literal|"hbase"
argument_list|,
literal|"default"
argument_list|)
expr_stmt|;
comment|// Delete namespace and check that REST still finds correct namespaces.
name|admin
operator|.
name|deleteNamespace
argument_list|(
name|NAMESPACE1
argument_list|)
expr_stmt|;
name|response
operator|=
name|client
operator|.
name|get
argument_list|(
name|namespacePath
argument_list|,
name|Constants
operator|.
name|MIMETYPE_XML
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|200
argument_list|,
name|response
operator|.
name|getCode
argument_list|()
argument_list|)
expr_stmt|;
name|model
operator|=
name|fromXML
argument_list|(
name|response
operator|.
name|getBody
argument_list|()
argument_list|)
expr_stmt|;
name|testNamespacesModel
operator|.
name|checkModel
argument_list|(
name|model
argument_list|,
name|NAMESPACE2
argument_list|,
literal|"hbase"
argument_list|,
literal|"default"
argument_list|)
expr_stmt|;
name|response
operator|=
name|client
operator|.
name|get
argument_list|(
name|namespacePath
argument_list|,
name|Constants
operator|.
name|MIMETYPE_JSON
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|200
argument_list|,
name|response
operator|.
name|getCode
argument_list|()
argument_list|)
expr_stmt|;
name|model
operator|=
name|testNamespacesModel
operator|.
name|fromJSON
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|response
operator|.
name|getBody
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|testNamespacesModel
operator|.
name|checkModel
argument_list|(
name|model
argument_list|,
name|NAMESPACE2
argument_list|,
literal|"hbase"
argument_list|,
literal|"default"
argument_list|)
expr_stmt|;
name|admin
operator|.
name|deleteNamespace
argument_list|(
name|NAMESPACE2
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testNamespaceListPBandDefault
parameter_list|()
throws|throws
name|IOException
throws|,
name|JAXBException
block|{
name|String
name|schemaPath
init|=
literal|"/namespaces/"
decl_stmt|;
name|NamespacesModel
name|model
decl_stmt|;
name|Response
name|response
decl_stmt|;
comment|// Check that namespace does not yet exist via non-REST call.
name|Admin
name|admin
init|=
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
decl_stmt|;
name|assertFalse
argument_list|(
name|doesNamespaceExist
argument_list|(
name|admin
argument_list|,
name|NAMESPACE1
argument_list|)
argument_list|)
expr_stmt|;
name|model
operator|=
name|testNamespacesModel
operator|.
name|buildTestModel
argument_list|()
expr_stmt|;
name|testNamespacesModel
operator|.
name|checkModel
argument_list|(
name|model
argument_list|)
expr_stmt|;
comment|// Check that REST GET finds only default namespaces via PB and default Accept header.
name|response
operator|=
name|client
operator|.
name|get
argument_list|(
name|schemaPath
argument_list|,
name|Constants
operator|.
name|MIMETYPE_PROTOBUF
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|200
argument_list|,
name|response
operator|.
name|getCode
argument_list|()
argument_list|)
expr_stmt|;
name|model
operator|.
name|getObjectFromMessage
argument_list|(
name|response
operator|.
name|getBody
argument_list|()
argument_list|)
expr_stmt|;
name|testNamespacesModel
operator|.
name|checkModel
argument_list|(
name|model
argument_list|,
literal|"hbase"
argument_list|,
literal|"default"
argument_list|)
expr_stmt|;
name|response
operator|=
name|client
operator|.
name|get
argument_list|(
name|schemaPath
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|200
argument_list|,
name|response
operator|.
name|getCode
argument_list|()
argument_list|)
expr_stmt|;
comment|// Create namespace and check that REST GET finds one additional namespace.
name|createNamespaceViaAdmin
argument_list|(
name|admin
argument_list|,
name|NAMESPACE1
argument_list|)
expr_stmt|;
name|response
operator|=
name|client
operator|.
name|get
argument_list|(
name|schemaPath
argument_list|,
name|Constants
operator|.
name|MIMETYPE_PROTOBUF
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|200
argument_list|,
name|response
operator|.
name|getCode
argument_list|()
argument_list|)
expr_stmt|;
name|model
operator|.
name|getObjectFromMessage
argument_list|(
name|response
operator|.
name|getBody
argument_list|()
argument_list|)
expr_stmt|;
name|testNamespacesModel
operator|.
name|checkModel
argument_list|(
name|model
argument_list|,
name|NAMESPACE1
argument_list|,
literal|"hbase"
argument_list|,
literal|"default"
argument_list|)
expr_stmt|;
name|response
operator|=
name|client
operator|.
name|get
argument_list|(
name|schemaPath
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|200
argument_list|,
name|response
operator|.
name|getCode
argument_list|()
argument_list|)
expr_stmt|;
comment|// Create another namespace and check that REST GET finds one additional namespace.
name|createNamespaceViaAdmin
argument_list|(
name|admin
argument_list|,
name|NAMESPACE2
argument_list|)
expr_stmt|;
name|response
operator|=
name|client
operator|.
name|get
argument_list|(
name|schemaPath
argument_list|,
name|Constants
operator|.
name|MIMETYPE_PROTOBUF
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|200
argument_list|,
name|response
operator|.
name|getCode
argument_list|()
argument_list|)
expr_stmt|;
name|model
operator|.
name|getObjectFromMessage
argument_list|(
name|response
operator|.
name|getBody
argument_list|()
argument_list|)
expr_stmt|;
name|testNamespacesModel
operator|.
name|checkModel
argument_list|(
name|model
argument_list|,
name|NAMESPACE1
argument_list|,
name|NAMESPACE2
argument_list|,
literal|"hbase"
argument_list|,
literal|"default"
argument_list|)
expr_stmt|;
name|response
operator|=
name|client
operator|.
name|get
argument_list|(
name|schemaPath
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|200
argument_list|,
name|response
operator|.
name|getCode
argument_list|()
argument_list|)
expr_stmt|;
comment|// Delete namespace and check that REST GET still finds correct namespaces.
name|admin
operator|.
name|deleteNamespace
argument_list|(
name|NAMESPACE1
argument_list|)
expr_stmt|;
name|response
operator|=
name|client
operator|.
name|get
argument_list|(
name|schemaPath
argument_list|,
name|Constants
operator|.
name|MIMETYPE_PROTOBUF
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|200
argument_list|,
name|response
operator|.
name|getCode
argument_list|()
argument_list|)
expr_stmt|;
name|model
operator|.
name|getObjectFromMessage
argument_list|(
name|response
operator|.
name|getBody
argument_list|()
argument_list|)
expr_stmt|;
name|testNamespacesModel
operator|.
name|checkModel
argument_list|(
name|model
argument_list|,
name|NAMESPACE2
argument_list|,
literal|"hbase"
argument_list|,
literal|"default"
argument_list|)
expr_stmt|;
name|response
operator|=
name|client
operator|.
name|get
argument_list|(
name|schemaPath
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|200
argument_list|,
name|response
operator|.
name|getCode
argument_list|()
argument_list|)
expr_stmt|;
name|admin
operator|.
name|deleteNamespace
argument_list|(
name|NAMESPACE2
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

