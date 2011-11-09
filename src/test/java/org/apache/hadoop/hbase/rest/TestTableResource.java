begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|java
operator|.
name|net
operator|.
name|InetSocketAddress
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
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
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|client
operator|.
name|HBaseAdmin
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
name|HTable
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
name|Put
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
name|TableModel
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
name|TableInfoModel
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
name|TableListModel
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
name|TableRegionModel
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
name|TestTableResource
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TestTableResource
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|String
name|TABLE
init|=
literal|"TestTableResource"
decl_stmt|;
specifier|private
specifier|static
name|String
name|COLUMN_FAMILY
init|=
literal|"test"
decl_stmt|;
specifier|private
specifier|static
name|String
name|COLUMN
init|=
name|COLUMN_FAMILY
operator|+
literal|":qualifier"
decl_stmt|;
specifier|private
specifier|static
name|Map
argument_list|<
name|HRegionInfo
argument_list|,
name|HServerAddress
argument_list|>
name|regionMap
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
argument_list|(
literal|3
argument_list|)
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
name|TableModel
operator|.
name|class
argument_list|,
name|TableInfoModel
operator|.
name|class
argument_list|,
name|TableListModel
operator|.
name|class
argument_list|,
name|TableRegionModel
operator|.
name|class
argument_list|)
expr_stmt|;
name|HBaseAdmin
name|admin
init|=
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
decl_stmt|;
if|if
condition|(
name|admin
operator|.
name|tableExists
argument_list|(
name|TABLE
argument_list|)
condition|)
block|{
return|return;
block|}
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TABLE
argument_list|)
decl_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|COLUMN_FAMILY
argument_list|)
argument_list|)
expr_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|htd
argument_list|)
expr_stmt|;
name|HTable
name|table
init|=
operator|new
name|HTable
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|TABLE
argument_list|)
decl_stmt|;
name|byte
index|[]
name|k
init|=
operator|new
name|byte
index|[
literal|3
index|]
decl_stmt|;
name|byte
index|[]
index|[]
name|famAndQf
init|=
name|KeyValue
operator|.
name|parseColumn
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|COLUMN
argument_list|)
argument_list|)
decl_stmt|;
for|for
control|(
name|byte
name|b1
init|=
literal|'a'
init|;
name|b1
operator|<
literal|'z'
condition|;
name|b1
operator|++
control|)
block|{
for|for
control|(
name|byte
name|b2
init|=
literal|'a'
init|;
name|b2
operator|<
literal|'z'
condition|;
name|b2
operator|++
control|)
block|{
for|for
control|(
name|byte
name|b3
init|=
literal|'a'
init|;
name|b3
operator|<
literal|'z'
condition|;
name|b3
operator|++
control|)
block|{
name|k
index|[
literal|0
index|]
operator|=
name|b1
expr_stmt|;
name|k
index|[
literal|1
index|]
operator|=
name|b2
expr_stmt|;
name|k
index|[
literal|2
index|]
operator|=
name|b3
expr_stmt|;
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|k
argument_list|)
decl_stmt|;
name|put
operator|.
name|setWriteToWAL
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|put
operator|.
name|add
argument_list|(
name|famAndQf
index|[
literal|0
index|]
argument_list|,
name|famAndQf
index|[
literal|1
index|]
argument_list|,
name|k
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|table
operator|.
name|flushCommits
argument_list|()
expr_stmt|;
comment|// get the initial layout (should just be one region)
name|Map
argument_list|<
name|HRegionInfo
argument_list|,
name|HServerAddress
argument_list|>
name|m
init|=
name|table
operator|.
name|getRegionsInfo
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|m
operator|.
name|size
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
comment|// tell the master to split the table
name|admin
operator|.
name|split
argument_list|(
name|TABLE
argument_list|)
expr_stmt|;
comment|// give some time for the split to happen
name|long
name|timeout
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|+
operator|(
literal|15
operator|*
literal|1000
operator|)
decl_stmt|;
while|while
condition|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|<
name|timeout
operator|&&
name|m
operator|.
name|size
argument_list|()
operator|!=
literal|2
condition|)
block|{
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|250
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// check again
name|m
operator|=
name|table
operator|.
name|getRegionsInfo
argument_list|()
expr_stmt|;
block|}
comment|// should have two regions now
name|assertEquals
argument_list|(
name|m
operator|.
name|size
argument_list|()
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|regionMap
operator|=
name|m
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"regions: "
operator|+
name|regionMap
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
name|void
name|checkTableList
parameter_list|(
name|TableListModel
name|model
parameter_list|)
block|{
name|boolean
name|found
init|=
literal|false
decl_stmt|;
name|Iterator
argument_list|<
name|TableModel
argument_list|>
name|tables
init|=
name|model
operator|.
name|getTables
argument_list|()
operator|.
name|iterator
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|tables
operator|.
name|hasNext
argument_list|()
argument_list|)
expr_stmt|;
while|while
condition|(
name|tables
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|TableModel
name|table
init|=
name|tables
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|table
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
name|TABLE
argument_list|)
condition|)
block|{
name|found
operator|=
literal|true
expr_stmt|;
break|break;
block|}
block|}
name|assertTrue
argument_list|(
name|found
argument_list|)
expr_stmt|;
block|}
name|void
name|checkTableInfo
parameter_list|(
name|TableInfoModel
name|model
parameter_list|)
block|{
name|assertEquals
argument_list|(
name|model
operator|.
name|getName
argument_list|()
argument_list|,
name|TABLE
argument_list|)
expr_stmt|;
name|Iterator
argument_list|<
name|TableRegionModel
argument_list|>
name|regions
init|=
name|model
operator|.
name|getRegions
argument_list|()
operator|.
name|iterator
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|regions
operator|.
name|hasNext
argument_list|()
argument_list|)
expr_stmt|;
while|while
condition|(
name|regions
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|TableRegionModel
name|region
init|=
name|regions
operator|.
name|next
argument_list|()
decl_stmt|;
name|boolean
name|found
init|=
literal|false
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|HRegionInfo
argument_list|,
name|HServerAddress
argument_list|>
name|e
range|:
name|regionMap
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|HRegionInfo
name|hri
init|=
name|e
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|String
name|hriRegionName
init|=
name|hri
operator|.
name|getRegionNameAsString
argument_list|()
decl_stmt|;
name|String
name|regionName
init|=
name|region
operator|.
name|getName
argument_list|()
decl_stmt|;
if|if
condition|(
name|hriRegionName
operator|.
name|equals
argument_list|(
name|regionName
argument_list|)
condition|)
block|{
name|found
operator|=
literal|true
expr_stmt|;
name|byte
index|[]
name|startKey
init|=
name|hri
operator|.
name|getStartKey
argument_list|()
decl_stmt|;
name|byte
index|[]
name|endKey
init|=
name|hri
operator|.
name|getEndKey
argument_list|()
decl_stmt|;
name|InetSocketAddress
name|sa
init|=
name|e
operator|.
name|getValue
argument_list|()
operator|.
name|getInetSocketAddress
argument_list|()
decl_stmt|;
name|String
name|location
init|=
name|sa
operator|.
name|getHostName
argument_list|()
operator|+
literal|":"
operator|+
name|Integer
operator|.
name|valueOf
argument_list|(
name|sa
operator|.
name|getPort
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|hri
operator|.
name|getRegionId
argument_list|()
argument_list|,
name|region
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|startKey
argument_list|,
name|region
operator|.
name|getStartKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|endKey
argument_list|,
name|region
operator|.
name|getEndKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|location
argument_list|,
name|region
operator|.
name|getLocation
argument_list|()
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
name|assertTrue
argument_list|(
name|found
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTableListText
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
literal|"/"
argument_list|,
name|Constants
operator|.
name|MIMETYPE_TEXT
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
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTableListXML
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
literal|"/"
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
name|TableListModel
name|model
init|=
operator|(
name|TableListModel
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
name|checkTableList
argument_list|(
name|model
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTableListJSON
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
literal|"/"
argument_list|,
name|Constants
operator|.
name|MIMETYPE_JSON
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
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTableListPB
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
literal|"/"
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
name|TableListModel
name|model
init|=
operator|new
name|TableListModel
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
name|checkTableList
argument_list|(
name|model
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTableInfoText
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
literal|"/"
operator|+
name|TABLE
operator|+
literal|"/regions"
argument_list|,
name|Constants
operator|.
name|MIMETYPE_TEXT
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
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTableInfoXML
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
literal|"/"
operator|+
name|TABLE
operator|+
literal|"/regions"
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
name|TableInfoModel
name|model
init|=
operator|(
name|TableInfoModel
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
name|checkTableInfo
argument_list|(
name|model
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTableInfoJSON
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
literal|"/"
operator|+
name|TABLE
operator|+
literal|"/regions"
argument_list|,
name|Constants
operator|.
name|MIMETYPE_JSON
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
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTableInfoPB
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
literal|"/"
operator|+
name|TABLE
operator|+
literal|"/regions"
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
name|TableInfoModel
name|model
init|=
operator|new
name|TableInfoModel
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
name|checkTableInfo
argument_list|(
name|model
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

