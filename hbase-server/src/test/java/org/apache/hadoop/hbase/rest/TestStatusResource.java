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
name|StorageClusterStatusModel
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
name|MediumTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestStatusResource
block|{
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|ROOT_REGION_NAME
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"-ROOT-,,0"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|META_REGION_NAME
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|".META.,,1"
argument_list|)
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
name|void
name|validate
parameter_list|(
name|StorageClusterStatusModel
name|model
parameter_list|)
block|{
name|assertNotNull
argument_list|(
name|model
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|model
operator|.
name|getRegions
argument_list|()
operator|>=
literal|1
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|model
operator|.
name|getRequests
argument_list|()
operator|>=
literal|0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|model
operator|.
name|getAverageLoad
argument_list|()
operator|>=
literal|0.0
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|model
operator|.
name|getLiveNodes
argument_list|()
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|model
operator|.
name|getDeadNodes
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|model
operator|.
name|getLiveNodes
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|boolean
name|foundRoot
init|=
literal|false
decl_stmt|,
name|foundMeta
init|=
literal|false
decl_stmt|;
for|for
control|(
name|StorageClusterStatusModel
operator|.
name|Node
name|node
range|:
name|model
operator|.
name|getLiveNodes
argument_list|()
control|)
block|{
name|assertNotNull
argument_list|(
name|node
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|node
operator|.
name|getStartCode
argument_list|()
operator|>
literal|0L
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|node
operator|.
name|getRequests
argument_list|()
operator|>=
literal|0
argument_list|)
expr_stmt|;
for|for
control|(
name|StorageClusterStatusModel
operator|.
name|Node
operator|.
name|Region
name|region
range|:
name|node
operator|.
name|getRegions
argument_list|()
control|)
block|{
if|if
condition|(
name|Bytes
operator|.
name|equals
argument_list|(
name|region
operator|.
name|getName
argument_list|()
argument_list|,
name|ROOT_REGION_NAME
argument_list|)
condition|)
block|{
name|foundRoot
operator|=
literal|true
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|Bytes
operator|.
name|equals
argument_list|(
name|region
operator|.
name|getName
argument_list|()
argument_list|,
name|META_REGION_NAME
argument_list|)
condition|)
block|{
name|foundMeta
operator|=
literal|true
expr_stmt|;
block|}
block|}
block|}
name|assertTrue
argument_list|(
name|foundRoot
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|foundMeta
argument_list|)
expr_stmt|;
block|}
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
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|()
expr_stmt|;
name|REST_TEST_UTIL
operator|.
name|startServletContainer
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
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
name|context
operator|=
name|JAXBContext
operator|.
name|newInstance
argument_list|(
name|StorageClusterStatusModel
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
annotation|@
name|Test
specifier|public
name|void
name|testGetClusterStatusXML
parameter_list|()
throws|throws
name|IOException
throws|,
name|JAXBException
block|{
name|Response
name|response
init|=
name|client
operator|.
name|get
argument_list|(
literal|"/status/cluster"
argument_list|,
name|Constants
operator|.
name|MIMETYPE_XML
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|response
operator|.
name|getCode
argument_list|()
argument_list|,
literal|200
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Constants
operator|.
name|MIMETYPE_XML
argument_list|,
name|response
operator|.
name|getHeader
argument_list|(
literal|"content-type"
argument_list|)
argument_list|)
expr_stmt|;
name|StorageClusterStatusModel
name|model
init|=
operator|(
name|StorageClusterStatusModel
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
name|response
operator|.
name|getBody
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|validate
argument_list|(
name|model
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetClusterStatusPB
parameter_list|()
throws|throws
name|IOException
block|{
name|Response
name|response
init|=
name|client
operator|.
name|get
argument_list|(
literal|"/status/cluster"
argument_list|,
name|Constants
operator|.
name|MIMETYPE_PROTOBUF
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|response
operator|.
name|getCode
argument_list|()
argument_list|,
literal|200
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Constants
operator|.
name|MIMETYPE_PROTOBUF
argument_list|,
name|response
operator|.
name|getHeader
argument_list|(
literal|"content-type"
argument_list|)
argument_list|)
expr_stmt|;
name|StorageClusterStatusModel
name|model
init|=
operator|new
name|StorageClusterStatusModel
argument_list|()
decl_stmt|;
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
name|validate
argument_list|(
name|model
argument_list|)
expr_stmt|;
name|response
operator|=
name|client
operator|.
name|get
argument_list|(
literal|"/status/cluster"
argument_list|,
name|Constants
operator|.
name|MIMETYPE_PROTOBUF_IETF
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|response
operator|.
name|getCode
argument_list|()
argument_list|,
literal|200
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Constants
operator|.
name|MIMETYPE_PROTOBUF_IETF
argument_list|,
name|response
operator|.
name|getHeader
argument_list|(
literal|"content-type"
argument_list|)
argument_list|)
expr_stmt|;
name|model
operator|=
operator|new
name|StorageClusterStatusModel
argument_list|()
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
name|validate
argument_list|(
name|model
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

