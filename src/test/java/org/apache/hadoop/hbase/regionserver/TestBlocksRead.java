begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright 2011 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
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
name|fs
operator|.
name|Path
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
name|HBaseTestCase
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
name|HColumnDescriptor
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
name|HRegionInfo
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
name|HTableDescriptor
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
name|KeyValue
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
name|Delete
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
name|Get
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
name|Scan
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
name|io
operator|.
name|hfile
operator|.
name|BlockCache
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
name|io
operator|.
name|hfile
operator|.
name|CacheConfig
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
name|io
operator|.
name|hfile
operator|.
name|HFile
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
name|io
operator|.
name|hfile
operator|.
name|LruBlockCache
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
name|EnvironmentEdgeManagerTestHelper
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

begin_class
specifier|public
class|class
name|TestBlocksRead
extends|extends
name|HBaseTestCase
block|{
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TestBlocksRead
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|String
index|[]
name|BLOOM_TYPE
init|=
operator|new
name|String
index|[]
block|{
literal|"ROWCOL"
block|,
literal|"ROW"
block|,
literal|"NONE"
block|}
decl_stmt|;
specifier|private
specifier|static
name|BlockCache
name|blockCache
decl_stmt|;
specifier|private
name|HBaseConfiguration
name|getConf
parameter_list|()
block|{
name|HBaseConfiguration
name|conf
init|=
operator|new
name|HBaseConfiguration
argument_list|()
decl_stmt|;
comment|// disable compactions in this test.
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.hstore.compactionThreshold"
argument_list|,
literal|10000
argument_list|)
expr_stmt|;
return|return
name|conf
return|;
block|}
name|HRegion
name|region
init|=
literal|null
decl_stmt|;
specifier|private
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|String
name|DIR
init|=
name|TEST_UTIL
operator|.
name|getDataTestDir
argument_list|(
literal|"TestBlocksRead"
argument_list|)
operator|.
name|toString
argument_list|()
decl_stmt|;
comment|/**    * @see org.apache.hadoop.hbase.HBaseTestCase#setUp()    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"deprecation"
argument_list|)
annotation|@
name|Override
specifier|protected
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|setUp
argument_list|()
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"deprecation"
argument_list|)
annotation|@
name|Override
specifier|protected
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|tearDown
argument_list|()
expr_stmt|;
name|EnvironmentEdgeManagerTestHelper
operator|.
name|reset
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|initHRegion
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|,
name|String
name|callingMethod
parameter_list|,
name|HBaseConfiguration
name|conf
parameter_list|,
name|String
name|family
parameter_list|)
throws|throws
name|IOException
block|{
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|HColumnDescriptor
name|familyDesc
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
name|BLOOM_TYPE
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|String
name|bloomType
init|=
name|BLOOM_TYPE
index|[
name|i
index|]
decl_stmt|;
name|familyDesc
operator|=
operator|new
name|HColumnDescriptor
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|family
operator|+
literal|"_"
operator|+
name|bloomType
argument_list|)
argument_list|,
name|HColumnDescriptor
operator|.
name|DEFAULT_VERSIONS
argument_list|,
name|HColumnDescriptor
operator|.
name|DEFAULT_COMPRESSION
argument_list|,
name|HColumnDescriptor
operator|.
name|DEFAULT_IN_MEMORY
argument_list|,
name|HColumnDescriptor
operator|.
name|DEFAULT_BLOCKCACHE
argument_list|,
literal|1
argument_list|,
comment|// small block size deliberate; each kv on its own block
name|HColumnDescriptor
operator|.
name|DEFAULT_TTL
argument_list|,
name|BLOOM_TYPE
index|[
name|i
index|]
argument_list|,
name|HColumnDescriptor
operator|.
name|DEFAULT_REPLICATION_SCOPE
argument_list|)
expr_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
name|familyDesc
argument_list|)
expr_stmt|;
block|}
name|HRegionInfo
name|info
init|=
operator|new
name|HRegionInfo
argument_list|(
name|htd
operator|.
name|getName
argument_list|()
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|Path
name|path
init|=
operator|new
name|Path
argument_list|(
name|DIR
operator|+
name|callingMethod
argument_list|)
decl_stmt|;
name|region
operator|=
name|HRegion
operator|.
name|createHRegion
argument_list|(
name|info
argument_list|,
name|path
argument_list|,
name|conf
argument_list|,
name|htd
argument_list|)
expr_stmt|;
name|blockCache
operator|=
operator|new
name|CacheConfig
argument_list|(
name|conf
argument_list|)
operator|.
name|getBlockCache
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|putData
parameter_list|(
name|String
name|family
parameter_list|,
name|String
name|row
parameter_list|,
name|String
name|col
parameter_list|,
name|long
name|version
parameter_list|)
throws|throws
name|IOException
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
name|BLOOM_TYPE
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|putData
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|family
operator|+
literal|"_"
operator|+
name|BLOOM_TYPE
index|[
name|i
index|]
argument_list|)
argument_list|,
name|row
argument_list|,
name|col
argument_list|,
name|version
argument_list|,
name|version
argument_list|)
expr_stmt|;
block|}
block|}
comment|// generates a value to put for a row/col/version.
specifier|private
specifier|static
name|byte
index|[]
name|genValue
parameter_list|(
name|String
name|row
parameter_list|,
name|String
name|col
parameter_list|,
name|long
name|version
parameter_list|)
block|{
return|return
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"Value:"
operator|+
name|row
operator|+
literal|"#"
operator|+
name|col
operator|+
literal|"#"
operator|+
name|version
argument_list|)
return|;
block|}
specifier|private
name|void
name|putData
parameter_list|(
name|byte
index|[]
name|cf
parameter_list|,
name|String
name|row
parameter_list|,
name|String
name|col
parameter_list|,
name|long
name|versionStart
parameter_list|,
name|long
name|versionEnd
parameter_list|)
throws|throws
name|IOException
block|{
name|byte
name|columnBytes
index|[]
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|col
argument_list|)
decl_stmt|;
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|row
argument_list|)
argument_list|)
decl_stmt|;
for|for
control|(
name|long
name|version
init|=
name|versionStart
init|;
name|version
operator|<=
name|versionEnd
condition|;
name|version
operator|++
control|)
block|{
name|put
operator|.
name|add
argument_list|(
name|cf
argument_list|,
name|columnBytes
argument_list|,
name|version
argument_list|,
name|genValue
argument_list|(
name|row
argument_list|,
name|col
argument_list|,
name|version
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|region
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
block|}
specifier|private
name|KeyValue
index|[]
name|getData
parameter_list|(
name|String
name|family
parameter_list|,
name|String
name|row
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|columns
parameter_list|,
name|int
name|expBlocks
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|getData
argument_list|(
name|family
argument_list|,
name|row
argument_list|,
name|columns
argument_list|,
name|expBlocks
argument_list|,
name|expBlocks
argument_list|,
name|expBlocks
argument_list|)
return|;
block|}
specifier|private
name|KeyValue
index|[]
name|getData
parameter_list|(
name|String
name|family
parameter_list|,
name|String
name|row
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|columns
parameter_list|,
name|int
name|expBlocksRowCol
parameter_list|,
name|int
name|expBlocksRow
parameter_list|,
name|int
name|expBlocksNone
parameter_list|)
throws|throws
name|IOException
block|{
name|int
index|[]
name|expBlocks
init|=
operator|new
name|int
index|[]
block|{
name|expBlocksRowCol
block|,
name|expBlocksRow
block|,
name|expBlocksNone
block|}
decl_stmt|;
name|KeyValue
index|[]
name|kvs
init|=
literal|null
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
name|BLOOM_TYPE
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|String
name|bloomType
init|=
name|BLOOM_TYPE
index|[
name|i
index|]
decl_stmt|;
name|byte
index|[]
name|cf
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|family
operator|+
literal|"_"
operator|+
name|bloomType
argument_list|)
decl_stmt|;
name|long
name|blocksStart
init|=
name|getBlkAccessCount
argument_list|(
name|cf
argument_list|)
decl_stmt|;
name|Get
name|get
init|=
operator|new
name|Get
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|row
argument_list|)
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|column
range|:
name|columns
control|)
block|{
name|get
operator|.
name|addColumn
argument_list|(
name|cf
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|column
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|kvs
operator|=
name|region
operator|.
name|get
argument_list|(
name|get
argument_list|,
literal|null
argument_list|)
operator|.
name|raw
argument_list|()
expr_stmt|;
name|long
name|blocksEnd
init|=
name|getBlkAccessCount
argument_list|(
name|cf
argument_list|)
decl_stmt|;
if|if
condition|(
name|expBlocks
index|[
name|i
index|]
operator|!=
operator|-
literal|1
condition|)
block|{
name|assertEquals
argument_list|(
literal|"Blocks Read Check for Bloom: "
operator|+
name|bloomType
argument_list|,
name|expBlocks
index|[
name|i
index|]
argument_list|,
name|blocksEnd
operator|-
name|blocksStart
argument_list|)
expr_stmt|;
block|}
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Blocks Read for Bloom: "
operator|+
name|bloomType
operator|+
literal|" = "
operator|+
operator|(
name|blocksEnd
operator|-
name|blocksStart
operator|)
operator|+
literal|"Expected = "
operator|+
name|expBlocks
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
return|return
name|kvs
return|;
block|}
specifier|private
name|KeyValue
index|[]
name|getData
parameter_list|(
name|String
name|family
parameter_list|,
name|String
name|row
parameter_list|,
name|String
name|column
parameter_list|,
name|int
name|expBlocks
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|getData
argument_list|(
name|family
argument_list|,
name|row
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|column
argument_list|)
argument_list|,
name|expBlocks
argument_list|,
name|expBlocks
argument_list|,
name|expBlocks
argument_list|)
return|;
block|}
specifier|private
name|KeyValue
index|[]
name|getData
parameter_list|(
name|String
name|family
parameter_list|,
name|String
name|row
parameter_list|,
name|String
name|column
parameter_list|,
name|int
name|expBlocksRowCol
parameter_list|,
name|int
name|expBlocksRow
parameter_list|,
name|int
name|expBlocksNone
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|getData
argument_list|(
name|family
argument_list|,
name|row
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|column
argument_list|)
argument_list|,
name|expBlocksRowCol
argument_list|,
name|expBlocksRow
argument_list|,
name|expBlocksNone
argument_list|)
return|;
block|}
specifier|private
name|void
name|deleteFamily
parameter_list|(
name|String
name|family
parameter_list|,
name|String
name|row
parameter_list|,
name|long
name|version
parameter_list|)
throws|throws
name|IOException
block|{
name|Delete
name|del
init|=
operator|new
name|Delete
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|row
argument_list|)
argument_list|)
decl_stmt|;
name|del
operator|.
name|deleteFamily
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|family
operator|+
literal|"_ROWCOL"
argument_list|)
argument_list|,
name|version
argument_list|)
expr_stmt|;
name|del
operator|.
name|deleteFamily
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|family
operator|+
literal|"_ROW"
argument_list|)
argument_list|,
name|version
argument_list|)
expr_stmt|;
name|del
operator|.
name|deleteFamily
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|family
operator|+
literal|"_NONE"
argument_list|)
argument_list|,
name|version
argument_list|)
expr_stmt|;
name|region
operator|.
name|delete
argument_list|(
name|del
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|void
name|verifyData
parameter_list|(
name|KeyValue
name|kv
parameter_list|,
name|String
name|expectedRow
parameter_list|,
name|String
name|expectedCol
parameter_list|,
name|long
name|expectedVersion
parameter_list|)
block|{
name|assertEquals
argument_list|(
literal|"RowCheck"
argument_list|,
name|expectedRow
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|kv
operator|.
name|getRow
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"ColumnCheck"
argument_list|,
name|expectedCol
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|kv
operator|.
name|getQualifier
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"TSCheck"
argument_list|,
name|expectedVersion
argument_list|,
name|kv
operator|.
name|getTimestamp
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"ValueCheck"
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|genValue
argument_list|(
name|expectedRow
argument_list|,
name|expectedCol
argument_list|,
name|expectedVersion
argument_list|)
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|kv
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|long
name|getBlkAccessCount
parameter_list|(
name|byte
index|[]
name|cf
parameter_list|)
block|{
return|return
name|HFile
operator|.
name|dataBlockReadCnt
operator|.
name|get
argument_list|()
return|;
block|}
specifier|private
specifier|static
name|long
name|getBlkCount
parameter_list|()
block|{
return|return
name|blockCache
operator|.
name|getBlockCount
argument_list|()
return|;
block|}
comment|/**    * Test # of blocks read for some simple seek cases.    *    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testBlocksRead
parameter_list|()
throws|throws
name|Exception
block|{
name|byte
index|[]
name|TABLE
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testBlocksRead"
argument_list|)
decl_stmt|;
name|String
name|FAMILY
init|=
literal|"cf1"
decl_stmt|;
name|KeyValue
name|kvs
index|[]
decl_stmt|;
name|HBaseConfiguration
name|conf
init|=
name|getConf
argument_list|()
decl_stmt|;
name|initHRegion
argument_list|(
name|TABLE
argument_list|,
name|getName
argument_list|()
argument_list|,
name|conf
argument_list|,
name|FAMILY
argument_list|)
expr_stmt|;
name|putData
argument_list|(
name|FAMILY
argument_list|,
literal|"row"
argument_list|,
literal|"col1"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|putData
argument_list|(
name|FAMILY
argument_list|,
literal|"row"
argument_list|,
literal|"col2"
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|putData
argument_list|(
name|FAMILY
argument_list|,
literal|"row"
argument_list|,
literal|"col3"
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|putData
argument_list|(
name|FAMILY
argument_list|,
literal|"row"
argument_list|,
literal|"col4"
argument_list|,
literal|4
argument_list|)
expr_stmt|;
name|putData
argument_list|(
name|FAMILY
argument_list|,
literal|"row"
argument_list|,
literal|"col5"
argument_list|,
literal|5
argument_list|)
expr_stmt|;
name|putData
argument_list|(
name|FAMILY
argument_list|,
literal|"row"
argument_list|,
literal|"col6"
argument_list|,
literal|6
argument_list|)
expr_stmt|;
name|putData
argument_list|(
name|FAMILY
argument_list|,
literal|"row"
argument_list|,
literal|"col7"
argument_list|,
literal|7
argument_list|)
expr_stmt|;
name|region
operator|.
name|flushcache
argument_list|()
expr_stmt|;
comment|// Expected block reads: 1
comment|// The top block has the KV we are
comment|// interested. So only 1 seek is needed.
name|kvs
operator|=
name|getData
argument_list|(
name|FAMILY
argument_list|,
literal|"row"
argument_list|,
literal|"col1"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|kvs
operator|.
name|length
argument_list|)
expr_stmt|;
name|verifyData
argument_list|(
name|kvs
index|[
literal|0
index|]
argument_list|,
literal|"row"
argument_list|,
literal|"col1"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
comment|// Expected block reads: 2
comment|// The top block and next block has the KVs we are
comment|// interested. So only 2 seek is needed.
name|kvs
operator|=
name|getData
argument_list|(
name|FAMILY
argument_list|,
literal|"row"
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
literal|"col1"
argument_list|,
literal|"col2"
argument_list|)
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|kvs
operator|.
name|length
argument_list|)
expr_stmt|;
name|verifyData
argument_list|(
name|kvs
index|[
literal|0
index|]
argument_list|,
literal|"row"
argument_list|,
literal|"col1"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|verifyData
argument_list|(
name|kvs
index|[
literal|1
index|]
argument_list|,
literal|"row"
argument_list|,
literal|"col2"
argument_list|,
literal|2
argument_list|)
expr_stmt|;
comment|// Expected block reads: 3
comment|// The first 2 seeks is to find out col2. [HBASE-4443]
comment|// One additional seek for col3
comment|// So 3 seeks are needed.
name|kvs
operator|=
name|getData
argument_list|(
name|FAMILY
argument_list|,
literal|"row"
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
literal|"col2"
argument_list|,
literal|"col3"
argument_list|)
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|kvs
operator|.
name|length
argument_list|)
expr_stmt|;
name|verifyData
argument_list|(
name|kvs
index|[
literal|0
index|]
argument_list|,
literal|"row"
argument_list|,
literal|"col2"
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|verifyData
argument_list|(
name|kvs
index|[
literal|1
index|]
argument_list|,
literal|"row"
argument_list|,
literal|"col3"
argument_list|,
literal|3
argument_list|)
expr_stmt|;
comment|// Expected block reads: 2. [HBASE-4443]
name|kvs
operator|=
name|getData
argument_list|(
name|FAMILY
argument_list|,
literal|"row"
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
literal|"col5"
argument_list|)
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|kvs
operator|.
name|length
argument_list|)
expr_stmt|;
name|verifyData
argument_list|(
name|kvs
index|[
literal|0
index|]
argument_list|,
literal|"row"
argument_list|,
literal|"col5"
argument_list|,
literal|5
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test # of blocks read (targetted at some of the cases Lazy Seek optimizes).    *    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testLazySeekBlocksRead
parameter_list|()
throws|throws
name|Exception
block|{
name|byte
index|[]
name|TABLE
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testLazySeekBlocksRead"
argument_list|)
decl_stmt|;
name|String
name|FAMILY
init|=
literal|"cf1"
decl_stmt|;
name|KeyValue
name|kvs
index|[]
decl_stmt|;
name|HBaseConfiguration
name|conf
init|=
name|getConf
argument_list|()
decl_stmt|;
name|initHRegion
argument_list|(
name|TABLE
argument_list|,
name|getName
argument_list|()
argument_list|,
name|conf
argument_list|,
name|FAMILY
argument_list|)
expr_stmt|;
comment|// File 1
name|putData
argument_list|(
name|FAMILY
argument_list|,
literal|"row"
argument_list|,
literal|"col1"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|putData
argument_list|(
name|FAMILY
argument_list|,
literal|"row"
argument_list|,
literal|"col2"
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|region
operator|.
name|flushcache
argument_list|()
expr_stmt|;
comment|// File 2
name|putData
argument_list|(
name|FAMILY
argument_list|,
literal|"row"
argument_list|,
literal|"col1"
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|putData
argument_list|(
name|FAMILY
argument_list|,
literal|"row"
argument_list|,
literal|"col2"
argument_list|,
literal|4
argument_list|)
expr_stmt|;
name|region
operator|.
name|flushcache
argument_list|()
expr_stmt|;
comment|// Expected blocks read: 1.
comment|// File 2's top block is also the KV we are
comment|// interested. So only 1 seek is needed.
name|kvs
operator|=
name|getData
argument_list|(
name|FAMILY
argument_list|,
literal|"row"
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
literal|"col1"
argument_list|)
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|kvs
operator|.
name|length
argument_list|)
expr_stmt|;
name|verifyData
argument_list|(
name|kvs
index|[
literal|0
index|]
argument_list|,
literal|"row"
argument_list|,
literal|"col1"
argument_list|,
literal|3
argument_list|)
expr_stmt|;
comment|// Expected blocks read: 2
comment|// File 2's top block has the "col1" KV we are
comment|// interested. We also need "col2" which is in a block
comment|// of its own. So, we need that block as well.
name|kvs
operator|=
name|getData
argument_list|(
name|FAMILY
argument_list|,
literal|"row"
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
literal|"col1"
argument_list|,
literal|"col2"
argument_list|)
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|kvs
operator|.
name|length
argument_list|)
expr_stmt|;
name|verifyData
argument_list|(
name|kvs
index|[
literal|0
index|]
argument_list|,
literal|"row"
argument_list|,
literal|"col1"
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|verifyData
argument_list|(
name|kvs
index|[
literal|1
index|]
argument_list|,
literal|"row"
argument_list|,
literal|"col2"
argument_list|,
literal|4
argument_list|)
expr_stmt|;
comment|// File 3: Add another column
name|putData
argument_list|(
name|FAMILY
argument_list|,
literal|"row"
argument_list|,
literal|"col3"
argument_list|,
literal|5
argument_list|)
expr_stmt|;
name|region
operator|.
name|flushcache
argument_list|()
expr_stmt|;
comment|// Expected blocks read: 1
comment|// File 3's top block has the "col3" KV we are
comment|// interested. So only 1 seek is needed.
name|kvs
operator|=
name|getData
argument_list|(
name|FAMILY
argument_list|,
literal|"row"
argument_list|,
literal|"col3"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|kvs
operator|.
name|length
argument_list|)
expr_stmt|;
name|verifyData
argument_list|(
name|kvs
index|[
literal|0
index|]
argument_list|,
literal|"row"
argument_list|,
literal|"col3"
argument_list|,
literal|5
argument_list|)
expr_stmt|;
comment|// Get a column from older file.
comment|// For ROWCOL Bloom filter: Expected blocks read: 1.
comment|// For ROW Bloom filter: Expected blocks read: 2.
comment|// For NONE Bloom filter: Expected blocks read: 2.
name|kvs
operator|=
name|getData
argument_list|(
name|FAMILY
argument_list|,
literal|"row"
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
literal|"col1"
argument_list|)
argument_list|,
literal|1
argument_list|,
literal|2
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|kvs
operator|.
name|length
argument_list|)
expr_stmt|;
name|verifyData
argument_list|(
name|kvs
index|[
literal|0
index|]
argument_list|,
literal|"row"
argument_list|,
literal|"col1"
argument_list|,
literal|3
argument_list|)
expr_stmt|;
comment|// File 4: Delete the entire row.
name|deleteFamily
argument_list|(
name|FAMILY
argument_list|,
literal|"row"
argument_list|,
literal|6
argument_list|)
expr_stmt|;
name|region
operator|.
name|flushcache
argument_list|()
expr_stmt|;
comment|// For ROWCOL Bloom filter: Expected blocks read: 2.
comment|// For ROW Bloom filter: Expected blocks read: 3.
comment|// For NONE Bloom filter: Expected blocks read: 3.
name|kvs
operator|=
name|getData
argument_list|(
name|FAMILY
argument_list|,
literal|"row"
argument_list|,
literal|"col1"
argument_list|,
literal|2
argument_list|,
literal|3
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|kvs
operator|.
name|length
argument_list|)
expr_stmt|;
name|kvs
operator|=
name|getData
argument_list|(
name|FAMILY
argument_list|,
literal|"row"
argument_list|,
literal|"col2"
argument_list|,
literal|3
argument_list|,
literal|4
argument_list|,
literal|4
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|kvs
operator|.
name|length
argument_list|)
expr_stmt|;
name|kvs
operator|=
name|getData
argument_list|(
name|FAMILY
argument_list|,
literal|"row"
argument_list|,
literal|"col3"
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|kvs
operator|.
name|length
argument_list|)
expr_stmt|;
name|kvs
operator|=
name|getData
argument_list|(
name|FAMILY
argument_list|,
literal|"row"
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
literal|"col1"
argument_list|,
literal|"col2"
argument_list|,
literal|"col3"
argument_list|)
argument_list|,
literal|4
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|kvs
operator|.
name|length
argument_list|)
expr_stmt|;
comment|// File 5: Delete
name|deleteFamily
argument_list|(
name|FAMILY
argument_list|,
literal|"row"
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|region
operator|.
name|flushcache
argument_list|()
expr_stmt|;
comment|// File 6: some more puts, but with timestamps older than the
comment|// previous delete.
name|putData
argument_list|(
name|FAMILY
argument_list|,
literal|"row"
argument_list|,
literal|"col1"
argument_list|,
literal|7
argument_list|)
expr_stmt|;
name|putData
argument_list|(
name|FAMILY
argument_list|,
literal|"row"
argument_list|,
literal|"col2"
argument_list|,
literal|8
argument_list|)
expr_stmt|;
name|putData
argument_list|(
name|FAMILY
argument_list|,
literal|"row"
argument_list|,
literal|"col3"
argument_list|,
literal|9
argument_list|)
expr_stmt|;
name|region
operator|.
name|flushcache
argument_list|()
expr_stmt|;
comment|// Baseline expected blocks read: 8. [HBASE-4532]
name|kvs
operator|=
name|getData
argument_list|(
name|FAMILY
argument_list|,
literal|"row"
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
literal|"col1"
argument_list|,
literal|"col2"
argument_list|,
literal|"col3"
argument_list|)
argument_list|,
literal|5
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|kvs
operator|.
name|length
argument_list|)
expr_stmt|;
comment|// File 7: Put back new data
name|putData
argument_list|(
name|FAMILY
argument_list|,
literal|"row"
argument_list|,
literal|"col1"
argument_list|,
literal|11
argument_list|)
expr_stmt|;
name|putData
argument_list|(
name|FAMILY
argument_list|,
literal|"row"
argument_list|,
literal|"col2"
argument_list|,
literal|12
argument_list|)
expr_stmt|;
name|putData
argument_list|(
name|FAMILY
argument_list|,
literal|"row"
argument_list|,
literal|"col3"
argument_list|,
literal|13
argument_list|)
expr_stmt|;
name|region
operator|.
name|flushcache
argument_list|()
expr_stmt|;
comment|// Expected blocks read: 5. [HBASE-4585]
name|kvs
operator|=
name|getData
argument_list|(
name|FAMILY
argument_list|,
literal|"row"
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
literal|"col1"
argument_list|,
literal|"col2"
argument_list|,
literal|"col3"
argument_list|)
argument_list|,
literal|5
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|kvs
operator|.
name|length
argument_list|)
expr_stmt|;
name|verifyData
argument_list|(
name|kvs
index|[
literal|0
index|]
argument_list|,
literal|"row"
argument_list|,
literal|"col1"
argument_list|,
literal|11
argument_list|)
expr_stmt|;
name|verifyData
argument_list|(
name|kvs
index|[
literal|1
index|]
argument_list|,
literal|"row"
argument_list|,
literal|"col2"
argument_list|,
literal|12
argument_list|)
expr_stmt|;
name|verifyData
argument_list|(
name|kvs
index|[
literal|2
index|]
argument_list|,
literal|"row"
argument_list|,
literal|"col3"
argument_list|,
literal|13
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test # of blocks read to ensure disabling cache-fill on Scan works.    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testBlocksStoredWhenCachingDisabled
parameter_list|()
throws|throws
name|Exception
block|{
name|byte
index|[]
name|TABLE
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testBlocksReadWhenCachingDisabled"
argument_list|)
decl_stmt|;
name|String
name|FAMILY
init|=
literal|"cf1"
decl_stmt|;
name|HBaseConfiguration
name|conf
init|=
name|getConf
argument_list|()
decl_stmt|;
name|initHRegion
argument_list|(
name|TABLE
argument_list|,
name|getName
argument_list|()
argument_list|,
name|conf
argument_list|,
name|FAMILY
argument_list|)
expr_stmt|;
name|putData
argument_list|(
name|FAMILY
argument_list|,
literal|"row"
argument_list|,
literal|"col1"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|putData
argument_list|(
name|FAMILY
argument_list|,
literal|"row"
argument_list|,
literal|"col2"
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|region
operator|.
name|flushcache
argument_list|()
expr_stmt|;
comment|// Execute a scan with caching turned off
comment|// Expected blocks stored: 0
name|long
name|blocksStart
init|=
name|getBlkCount
argument_list|()
decl_stmt|;
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|scan
operator|.
name|setCacheBlocks
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|RegionScanner
name|rs
init|=
name|region
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|KeyValue
argument_list|>
name|result
init|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|(
literal|2
argument_list|)
decl_stmt|;
name|rs
operator|.
name|next
argument_list|(
name|result
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
operator|*
name|BLOOM_TYPE
operator|.
name|length
argument_list|,
name|result
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|rs
operator|.
name|close
argument_list|()
expr_stmt|;
name|long
name|blocksEnd
init|=
name|getBlkCount
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|blocksStart
argument_list|,
name|blocksEnd
argument_list|)
expr_stmt|;
comment|// Execute with caching turned on
comment|// Expected blocks stored: 2
name|blocksStart
operator|=
name|blocksEnd
expr_stmt|;
name|scan
operator|.
name|setCacheBlocks
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|rs
operator|=
name|region
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
expr_stmt|;
name|result
operator|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|rs
operator|.
name|next
argument_list|(
name|result
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
operator|*
name|BLOOM_TYPE
operator|.
name|length
argument_list|,
name|result
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|rs
operator|.
name|close
argument_list|()
expr_stmt|;
name|blocksEnd
operator|=
name|getBlkCount
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
operator|*
name|BLOOM_TYPE
operator|.
name|length
argument_list|,
name|blocksEnd
operator|-
name|blocksStart
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testLazySeekBlocksReadWithDelete
parameter_list|()
throws|throws
name|Exception
block|{
name|byte
index|[]
name|TABLE
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testLazySeekBlocksReadWithDelete"
argument_list|)
decl_stmt|;
name|String
name|FAMILY
init|=
literal|"cf1"
decl_stmt|;
name|KeyValue
name|kvs
index|[]
decl_stmt|;
name|HBaseConfiguration
name|conf
init|=
name|getConf
argument_list|()
decl_stmt|;
name|initHRegion
argument_list|(
name|TABLE
argument_list|,
name|getName
argument_list|()
argument_list|,
name|conf
argument_list|,
name|FAMILY
argument_list|)
expr_stmt|;
name|deleteFamily
argument_list|(
name|FAMILY
argument_list|,
literal|"row"
argument_list|,
literal|200
argument_list|)
expr_stmt|;
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
name|putData
argument_list|(
name|FAMILY
argument_list|,
literal|"row"
argument_list|,
literal|"col"
operator|+
name|i
argument_list|,
name|i
argument_list|)
expr_stmt|;
block|}
name|putData
argument_list|(
name|FAMILY
argument_list|,
literal|"row"
argument_list|,
literal|"col99"
argument_list|,
literal|201
argument_list|)
expr_stmt|;
name|region
operator|.
name|flushcache
argument_list|()
expr_stmt|;
name|kvs
operator|=
name|getData
argument_list|(
name|FAMILY
argument_list|,
literal|"row"
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
literal|"col0"
argument_list|)
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|kvs
operator|.
name|length
argument_list|)
expr_stmt|;
name|kvs
operator|=
name|getData
argument_list|(
name|FAMILY
argument_list|,
literal|"row"
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
literal|"col99"
argument_list|)
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|kvs
operator|.
name|length
argument_list|)
expr_stmt|;
name|verifyData
argument_list|(
name|kvs
index|[
literal|0
index|]
argument_list|,
literal|"row"
argument_list|,
literal|"col99"
argument_list|,
literal|201
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

