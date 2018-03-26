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
name|replication
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
name|assertNotEquals
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
name|junit
operator|.
name|Assert
operator|.
name|assertTrue
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
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|TimeUnit
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|Collectors
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
name|HConstants
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
name|TableName
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
name|ColumnFamilyDescriptorBuilder
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
name|client
operator|.
name|RegionInfo
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
name|Table
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
name|TableDescriptorBuilder
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
name|regionserver
operator|.
name|HRegionServer
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
name|ReplicationTests
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
name|hbase
operator|.
name|util
operator|.
name|CommonFSUtils
operator|.
name|StreamLacksCapabilityException
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
name|wal
operator|.
name|WAL
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
name|wal
operator|.
name|WAL
operator|.
name|Entry
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
name|wal
operator|.
name|WALFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
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
name|ReplicationTests
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
name|TestSerialReplication
extends|extends
name|SerialReplicationTestBase
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
name|TestSerialReplication
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|IOException
throws|,
name|StreamLacksCapabilityException
block|{
name|setupWALWriter
argument_list|()
expr_stmt|;
comment|// add in disable state, so later when enabling it all sources will start push together.
name|addPeer
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRegionMove
parameter_list|()
throws|throws
name|Exception
block|{
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
decl_stmt|;
name|UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|createTable
argument_list|(
name|TableDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|tableName
argument_list|)
operator|.
name|setColumnFamily
argument_list|(
name|ColumnFamilyDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|CF
argument_list|)
operator|.
name|setScope
argument_list|(
name|HConstants
operator|.
name|REPLICATION_SCOPE_GLOBAL
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|waitTableAvailable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
try|try
init|(
name|Table
name|table
init|=
name|UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
init|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|100
condition|;
name|i
operator|++
control|)
block|{
name|table
operator|.
name|put
argument_list|(
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
argument_list|)
operator|.
name|addColumn
argument_list|(
name|CF
argument_list|,
name|CQ
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|RegionInfo
name|region
init|=
name|UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|getRegions
argument_list|(
name|tableName
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|HRegionServer
name|rs
init|=
name|UTIL
operator|.
name|getOtherRegionServer
argument_list|(
name|UTIL
operator|.
name|getRSForFirstRegionInTable
argument_list|(
name|tableName
argument_list|)
argument_list|)
decl_stmt|;
name|moveRegion
argument_list|(
name|region
argument_list|,
name|rs
argument_list|)
expr_stmt|;
try|try
init|(
name|Table
name|table
init|=
name|UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
init|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|100
init|;
name|i
operator|<
literal|200
condition|;
name|i
operator|++
control|)
block|{
name|table
operator|.
name|put
argument_list|(
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
argument_list|)
operator|.
name|addColumn
argument_list|(
name|CF
argument_list|,
name|CQ
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|enablePeerAndWaitUntilReplicationDone
argument_list|(
literal|200
argument_list|)
expr_stmt|;
name|checkOrder
argument_list|(
literal|200
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRegionSplit
parameter_list|()
throws|throws
name|Exception
block|{
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
decl_stmt|;
name|UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|createTable
argument_list|(
name|TableDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|tableName
argument_list|)
operator|.
name|setColumnFamily
argument_list|(
name|ColumnFamilyDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|CF
argument_list|)
operator|.
name|setScope
argument_list|(
name|HConstants
operator|.
name|REPLICATION_SCOPE_GLOBAL
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|waitTableAvailable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
try|try
init|(
name|Table
name|table
init|=
name|UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
init|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|100
condition|;
name|i
operator|++
control|)
block|{
name|table
operator|.
name|put
argument_list|(
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
argument_list|)
operator|.
name|addColumn
argument_list|(
name|CF
argument_list|,
name|CQ
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|UTIL
operator|.
name|flush
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|RegionInfo
name|region
init|=
name|UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|getRegions
argument_list|(
name|tableName
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|splitRegionAsync
argument_list|(
name|region
operator|.
name|getEncodedNameAsBytes
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|50
argument_list|)
argument_list|)
operator|.
name|get
argument_list|(
literal|30
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|waitUntilNoRegionsInTransition
argument_list|(
literal|30000
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|regions
init|=
name|UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|getRegions
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|regions
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
try|try
init|(
name|Table
name|table
init|=
name|UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
init|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|100
condition|;
name|i
operator|++
control|)
block|{
name|table
operator|.
name|put
argument_list|(
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
argument_list|)
operator|.
name|addColumn
argument_list|(
name|CF
argument_list|,
name|CQ
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|enablePeerAndWaitUntilReplicationDone
argument_list|(
literal|200
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|regionsToSeqId
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|regionsToSeqId
operator|.
name|put
argument_list|(
name|region
operator|.
name|getEncodedName
argument_list|()
argument_list|,
operator|-
literal|1L
argument_list|)
expr_stmt|;
name|regions
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|RegionInfo
operator|::
name|getEncodedName
argument_list|)
operator|.
name|forEach
argument_list|(
name|n
lambda|->
name|regionsToSeqId
operator|.
name|put
argument_list|(
name|n
argument_list|,
operator|-
literal|1L
argument_list|)
argument_list|)
expr_stmt|;
try|try
init|(
name|WAL
operator|.
name|Reader
name|reader
init|=
name|WALFactory
operator|.
name|createReader
argument_list|(
name|UTIL
operator|.
name|getTestFileSystem
argument_list|()
argument_list|,
name|logPath
argument_list|,
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
init|)
block|{
name|int
name|count
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Entry
name|entry
init|;
condition|;
control|)
block|{
name|entry
operator|=
name|reader
operator|.
name|next
argument_list|()
expr_stmt|;
if|if
condition|(
name|entry
operator|==
literal|null
condition|)
block|{
break|break;
block|}
name|String
name|encodedName
init|=
name|Bytes
operator|.
name|toString
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|getEncodedRegionName
argument_list|()
argument_list|)
decl_stmt|;
name|Long
name|seqId
init|=
name|regionsToSeqId
operator|.
name|get
argument_list|(
name|encodedName
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
literal|"Unexcepted entry "
operator|+
name|entry
operator|+
literal|", expected regions "
operator|+
name|region
operator|+
literal|", or "
operator|+
name|regions
argument_list|,
name|seqId
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Sequence id go backwards from "
operator|+
name|seqId
operator|+
literal|" to "
operator|+
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|getSequenceId
argument_list|()
operator|+
literal|" for "
operator|+
name|encodedName
argument_list|,
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|getSequenceId
argument_list|()
operator|>=
name|seqId
operator|.
name|longValue
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|count
operator|<
literal|100
condition|)
block|{
name|assertEquals
argument_list|(
name|encodedName
operator|+
literal|" is pushed before parent "
operator|+
name|region
operator|.
name|getEncodedName
argument_list|()
argument_list|,
name|region
operator|.
name|getEncodedName
argument_list|()
argument_list|,
name|encodedName
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertNotEquals
argument_list|(
name|region
operator|.
name|getEncodedName
argument_list|()
argument_list|,
name|encodedName
argument_list|)
expr_stmt|;
block|}
name|count
operator|++
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|200
argument_list|,
name|count
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRegionMerge
parameter_list|()
throws|throws
name|Exception
block|{
name|byte
index|[]
name|splitKey
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|50
argument_list|)
decl_stmt|;
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
decl_stmt|;
name|UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|createTable
argument_list|(
name|TableDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|tableName
argument_list|)
operator|.
name|setColumnFamily
argument_list|(
name|ColumnFamilyDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|CF
argument_list|)
operator|.
name|setScope
argument_list|(
name|HConstants
operator|.
name|REPLICATION_SCOPE_GLOBAL
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
argument_list|,
operator|new
name|byte
index|[]
index|[]
block|{
name|splitKey
block|}
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|waitTableAvailable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
try|try
init|(
name|Table
name|table
init|=
name|UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
init|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|100
condition|;
name|i
operator|++
control|)
block|{
name|table
operator|.
name|put
argument_list|(
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
argument_list|)
operator|.
name|addColumn
argument_list|(
name|CF
argument_list|,
name|CQ
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|regions
init|=
name|UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|getRegions
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|mergeRegionsAsync
argument_list|(
name|regions
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|RegionInfo
operator|::
name|getEncodedNameAsBytes
argument_list|)
operator|.
name|toArray
argument_list|(
name|byte
index|[]
index|[]
operator|::
operator|new
argument_list|)
argument_list|,
literal|false
argument_list|)
operator|.
name|get
argument_list|(
literal|30
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|waitUntilNoRegionsInTransition
argument_list|(
literal|30000
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|regionsAfterMerge
init|=
name|UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|getRegions
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|regionsAfterMerge
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
try|try
init|(
name|Table
name|table
init|=
name|UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
init|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|100
condition|;
name|i
operator|++
control|)
block|{
name|table
operator|.
name|put
argument_list|(
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
argument_list|)
operator|.
name|addColumn
argument_list|(
name|CF
argument_list|,
name|CQ
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|enablePeerAndWaitUntilReplicationDone
argument_list|(
literal|200
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|regionsToSeqId
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|RegionInfo
name|region
init|=
name|regionsAfterMerge
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|regionsToSeqId
operator|.
name|put
argument_list|(
name|region
operator|.
name|getEncodedName
argument_list|()
argument_list|,
operator|-
literal|1L
argument_list|)
expr_stmt|;
name|regions
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|RegionInfo
operator|::
name|getEncodedName
argument_list|)
operator|.
name|forEach
argument_list|(
name|n
lambda|->
name|regionsToSeqId
operator|.
name|put
argument_list|(
name|n
argument_list|,
operator|-
literal|1L
argument_list|)
argument_list|)
expr_stmt|;
try|try
init|(
name|WAL
operator|.
name|Reader
name|reader
init|=
name|WALFactory
operator|.
name|createReader
argument_list|(
name|UTIL
operator|.
name|getTestFileSystem
argument_list|()
argument_list|,
name|logPath
argument_list|,
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
init|)
block|{
name|int
name|count
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Entry
name|entry
init|;
condition|;
control|)
block|{
name|entry
operator|=
name|reader
operator|.
name|next
argument_list|()
expr_stmt|;
if|if
condition|(
name|entry
operator|==
literal|null
condition|)
block|{
break|break;
block|}
name|String
name|encodedName
init|=
name|Bytes
operator|.
name|toString
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|getEncodedRegionName
argument_list|()
argument_list|)
decl_stmt|;
name|Long
name|seqId
init|=
name|regionsToSeqId
operator|.
name|get
argument_list|(
name|encodedName
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
literal|"Unexcepted entry "
operator|+
name|entry
operator|+
literal|", expected regions "
operator|+
name|region
operator|+
literal|", or "
operator|+
name|regions
argument_list|,
name|seqId
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Sequence id go backwards from "
operator|+
name|seqId
operator|+
literal|" to "
operator|+
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|getSequenceId
argument_list|()
operator|+
literal|" for "
operator|+
name|encodedName
argument_list|,
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|getSequenceId
argument_list|()
operator|>=
name|seqId
operator|.
name|longValue
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|count
operator|<
literal|100
condition|)
block|{
name|assertNotEquals
argument_list|(
name|encodedName
operator|+
literal|" is pushed before parents "
operator|+
name|regions
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|RegionInfo
operator|::
name|getEncodedName
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|joining
argument_list|(
literal|" and "
argument_list|)
argument_list|)
argument_list|,
name|region
operator|.
name|getEncodedName
argument_list|()
argument_list|,
name|encodedName
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertEquals
argument_list|(
name|region
operator|.
name|getEncodedName
argument_list|()
argument_list|,
name|encodedName
argument_list|)
expr_stmt|;
block|}
name|count
operator|++
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|200
argument_list|,
name|count
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

