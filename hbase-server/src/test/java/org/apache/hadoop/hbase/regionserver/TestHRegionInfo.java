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
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertArrayEquals
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
name|assertFalse
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
name|fs
operator|.
name|FileStatus
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
name|exceptions
operator|.
name|DeserializationException
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
name|master
operator|.
name|RegionState
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
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|UnsafeByteOperations
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|HBaseProtos
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|HBaseProtos
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
name|testclassification
operator|.
name|RegionServerTests
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
name|FSTableDescriptors
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
name|MD5Hash
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Assert
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Rule
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
name|junit
operator|.
name|rules
operator|.
name|TestName
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|RegionServerTests
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
name|TestHRegionInfo
block|{
annotation|@
name|Rule
specifier|public
name|TestName
name|name
init|=
operator|new
name|TestName
argument_list|()
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testPb
parameter_list|()
throws|throws
name|DeserializationException
block|{
name|HRegionInfo
name|hri
init|=
name|HRegionInfo
operator|.
name|FIRST_META_REGIONINFO
decl_stmt|;
name|byte
index|[]
name|bytes
init|=
name|hri
operator|.
name|toByteArray
argument_list|()
decl_stmt|;
name|HRegionInfo
name|pbhri
init|=
name|HRegionInfo
operator|.
name|parseFrom
argument_list|(
name|bytes
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|hri
operator|.
name|equals
argument_list|(
name|pbhri
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testReadAndWriteHRegionInfoFile
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|HBaseTestingUtility
name|htu
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
name|HRegionInfo
name|hri
init|=
name|HRegionInfo
operator|.
name|FIRST_META_REGIONINFO
decl_stmt|;
name|Path
name|basedir
init|=
name|htu
operator|.
name|getDataTestDir
argument_list|()
decl_stmt|;
comment|// Create a region.  That'll write the .regioninfo file.
name|FSTableDescriptors
name|fsTableDescriptors
init|=
operator|new
name|FSTableDescriptors
argument_list|(
name|htu
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|HRegion
name|r
init|=
name|HBaseTestingUtility
operator|.
name|createRegionAndWAL
argument_list|(
name|hri
argument_list|,
name|basedir
argument_list|,
name|htu
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|fsTableDescriptors
operator|.
name|get
argument_list|(
name|TableName
operator|.
name|META_TABLE_NAME
argument_list|)
argument_list|)
decl_stmt|;
comment|// Get modtime on the file.
name|long
name|modtime
init|=
name|getModTime
argument_list|(
name|r
argument_list|)
decl_stmt|;
name|HBaseTestingUtility
operator|.
name|closeRegionAndWAL
argument_list|(
name|r
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|1001
argument_list|)
expr_stmt|;
name|r
operator|=
name|HRegion
operator|.
name|openHRegion
argument_list|(
name|basedir
argument_list|,
name|hri
argument_list|,
name|fsTableDescriptors
operator|.
name|get
argument_list|(
name|TableName
operator|.
name|META_TABLE_NAME
argument_list|)
argument_list|,
literal|null
argument_list|,
name|htu
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
comment|// Ensure the file is not written for a second time.
name|long
name|modtime2
init|=
name|getModTime
argument_list|(
name|r
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|modtime
argument_list|,
name|modtime2
argument_list|)
expr_stmt|;
comment|// Now load the file.
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
name|deserializedHri
init|=
name|HRegionFileSystem
operator|.
name|loadRegionInfoFileContent
argument_list|(
name|r
operator|.
name|getRegionFileSystem
argument_list|()
operator|.
name|getFileSystem
argument_list|()
argument_list|,
name|r
operator|.
name|getRegionFileSystem
argument_list|()
operator|.
name|getRegionDir
argument_list|()
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
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
operator|.
name|COMPARATOR
operator|.
name|compare
argument_list|(
name|hri
argument_list|,
name|deserializedHri
argument_list|)
operator|==
literal|0
argument_list|)
expr_stmt|;
name|HBaseTestingUtility
operator|.
name|closeRegionAndWAL
argument_list|(
name|r
argument_list|)
expr_stmt|;
block|}
name|long
name|getModTime
parameter_list|(
specifier|final
name|HRegion
name|r
parameter_list|)
throws|throws
name|IOException
block|{
name|FileStatus
index|[]
name|statuses
init|=
name|r
operator|.
name|getRegionFileSystem
argument_list|()
operator|.
name|getFileSystem
argument_list|()
operator|.
name|listStatus
argument_list|(
operator|new
name|Path
argument_list|(
name|r
operator|.
name|getRegionFileSystem
argument_list|()
operator|.
name|getRegionDir
argument_list|()
argument_list|,
name|HRegionFileSystem
operator|.
name|REGION_INFO_FILE
argument_list|)
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|statuses
operator|!=
literal|null
operator|&&
name|statuses
operator|.
name|length
operator|==
literal|1
argument_list|)
expr_stmt|;
return|return
name|statuses
index|[
literal|0
index|]
operator|.
name|getModificationTime
argument_list|()
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCreateHRegionInfoName
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|String
name|tableName
init|=
name|name
operator|.
name|getMethodName
argument_list|()
decl_stmt|;
specifier|final
name|TableName
name|tn
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|String
name|startKey
init|=
literal|"startkey"
decl_stmt|;
specifier|final
name|byte
index|[]
name|sk
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|startKey
argument_list|)
decl_stmt|;
name|String
name|id
init|=
literal|"id"
decl_stmt|;
comment|// old format region name
name|byte
index|[]
name|name
init|=
name|HRegionInfo
operator|.
name|createRegionName
argument_list|(
name|tn
argument_list|,
name|sk
argument_list|,
name|id
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|String
name|nameStr
init|=
name|Bytes
operator|.
name|toString
argument_list|(
name|name
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|tableName
operator|+
literal|","
operator|+
name|startKey
operator|+
literal|","
operator|+
name|id
argument_list|,
name|nameStr
argument_list|)
expr_stmt|;
comment|// new format region name.
name|String
name|md5HashInHex
init|=
name|MD5Hash
operator|.
name|getMD5AsHex
argument_list|(
name|name
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|HRegionInfo
operator|.
name|MD5_HEX_LENGTH
argument_list|,
name|md5HashInHex
operator|.
name|length
argument_list|()
argument_list|)
expr_stmt|;
name|name
operator|=
name|HRegionInfo
operator|.
name|createRegionName
argument_list|(
name|tn
argument_list|,
name|sk
argument_list|,
name|id
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|nameStr
operator|=
name|Bytes
operator|.
name|toString
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|tableName
operator|+
literal|","
operator|+
name|startKey
operator|+
literal|","
operator|+
name|id
operator|+
literal|"."
operator|+
name|md5HashInHex
operator|+
literal|"."
argument_list|,
name|nameStr
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testContainsRange
parameter_list|()
block|{
name|HTableDescriptor
name|tableDesc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|HRegionInfo
name|hri
init|=
operator|new
name|HRegionInfo
argument_list|(
name|tableDesc
operator|.
name|getTableName
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"a"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"g"
argument_list|)
argument_list|)
decl_stmt|;
comment|// Single row range at start of region
name|assertTrue
argument_list|(
name|hri
operator|.
name|containsRange
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"a"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"a"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// Fully contained range
name|assertTrue
argument_list|(
name|hri
operator|.
name|containsRange
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"b"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"c"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// Range overlapping start of region
name|assertTrue
argument_list|(
name|hri
operator|.
name|containsRange
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"a"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"c"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// Fully contained single-row range
name|assertTrue
argument_list|(
name|hri
operator|.
name|containsRange
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"c"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"c"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// Range that overlaps end key and hence doesn't fit
name|assertFalse
argument_list|(
name|hri
operator|.
name|containsRange
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"a"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"g"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// Single row range on end key
name|assertFalse
argument_list|(
name|hri
operator|.
name|containsRange
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"g"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"g"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// Single row range entirely outside
name|assertFalse
argument_list|(
name|hri
operator|.
name|containsRange
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"z"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"z"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// Degenerate range
try|try
block|{
name|hri
operator|.
name|containsRange
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"z"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"a"
argument_list|)
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Invalid range did not throw IAE"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|iae
parameter_list|)
block|{     }
block|}
annotation|@
name|Test
specifier|public
name|void
name|testLastRegionCompare
parameter_list|()
block|{
name|HTableDescriptor
name|tableDesc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|HRegionInfo
name|hrip
init|=
operator|new
name|HRegionInfo
argument_list|(
name|tableDesc
operator|.
name|getTableName
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"a"
argument_list|)
argument_list|,
operator|new
name|byte
index|[
literal|0
index|]
argument_list|)
decl_stmt|;
name|HRegionInfo
name|hric
init|=
operator|new
name|HRegionInfo
argument_list|(
name|tableDesc
operator|.
name|getTableName
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"a"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"b"
argument_list|)
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|hrip
operator|.
name|compareTo
argument_list|(
name|hric
argument_list|)
operator|>
literal|0
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMetaTables
parameter_list|()
block|{
name|assertTrue
argument_list|(
name|HRegionInfo
operator|.
name|FIRST_META_REGIONINFO
operator|.
name|isMetaTable
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testComparator
parameter_list|()
block|{
specifier|final
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
name|byte
index|[]
name|empty
init|=
operator|new
name|byte
index|[
literal|0
index|]
decl_stmt|;
name|HRegionInfo
name|older
init|=
operator|new
name|HRegionInfo
argument_list|(
name|tableName
argument_list|,
name|empty
argument_list|,
name|empty
argument_list|,
literal|false
argument_list|,
literal|0L
argument_list|)
decl_stmt|;
name|HRegionInfo
name|newer
init|=
operator|new
name|HRegionInfo
argument_list|(
name|tableName
argument_list|,
name|empty
argument_list|,
name|empty
argument_list|,
literal|false
argument_list|,
literal|1L
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|older
operator|.
name|compareTo
argument_list|(
name|newer
argument_list|)
operator|<
literal|0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|newer
operator|.
name|compareTo
argument_list|(
name|older
argument_list|)
operator|>
literal|0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|older
operator|.
name|compareTo
argument_list|(
name|older
argument_list|)
operator|==
literal|0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|newer
operator|.
name|compareTo
argument_list|(
name|newer
argument_list|)
operator|==
literal|0
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRegionNameForRegionReplicas
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|tableName
init|=
name|name
operator|.
name|getMethodName
argument_list|()
decl_stmt|;
specifier|final
name|TableName
name|tn
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|String
name|startKey
init|=
literal|"startkey"
decl_stmt|;
specifier|final
name|byte
index|[]
name|sk
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|startKey
argument_list|)
decl_stmt|;
name|String
name|id
init|=
literal|"id"
decl_stmt|;
comment|// assert with only the region name without encoding
comment|// primary, replicaId = 0
name|byte
index|[]
name|name
init|=
name|HRegionInfo
operator|.
name|createRegionName
argument_list|(
name|tn
argument_list|,
name|sk
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|id
argument_list|)
argument_list|,
literal|0
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|String
name|nameStr
init|=
name|Bytes
operator|.
name|toString
argument_list|(
name|name
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|tableName
operator|+
literal|","
operator|+
name|startKey
operator|+
literal|","
operator|+
name|id
argument_list|,
name|nameStr
argument_list|)
expr_stmt|;
comment|// replicaId = 1
name|name
operator|=
name|HRegionInfo
operator|.
name|createRegionName
argument_list|(
name|tn
argument_list|,
name|sk
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|id
argument_list|)
argument_list|,
literal|1
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|nameStr
operator|=
name|Bytes
operator|.
name|toString
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|tableName
operator|+
literal|","
operator|+
name|startKey
operator|+
literal|","
operator|+
name|id
operator|+
literal|"_"
operator|+
name|String
operator|.
name|format
argument_list|(
name|HRegionInfo
operator|.
name|REPLICA_ID_FORMAT
argument_list|,
literal|1
argument_list|)
argument_list|,
name|nameStr
argument_list|)
expr_stmt|;
comment|// replicaId = max
name|name
operator|=
name|HRegionInfo
operator|.
name|createRegionName
argument_list|(
name|tn
argument_list|,
name|sk
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|id
argument_list|)
argument_list|,
literal|0xFFFF
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|nameStr
operator|=
name|Bytes
operator|.
name|toString
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|tableName
operator|+
literal|","
operator|+
name|startKey
operator|+
literal|","
operator|+
name|id
operator|+
literal|"_"
operator|+
name|String
operator|.
name|format
argument_list|(
name|HRegionInfo
operator|.
name|REPLICA_ID_FORMAT
argument_list|,
literal|0xFFFF
argument_list|)
argument_list|,
name|nameStr
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testParseName
parameter_list|()
throws|throws
name|IOException
block|{
specifier|final
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
name|byte
index|[]
name|startKey
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"startKey"
argument_list|)
decl_stmt|;
name|long
name|regionId
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|int
name|replicaId
init|=
literal|42
decl_stmt|;
comment|// test without replicaId
name|byte
index|[]
name|regionName
init|=
name|HRegionInfo
operator|.
name|createRegionName
argument_list|(
name|tableName
argument_list|,
name|startKey
argument_list|,
name|regionId
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|byte
index|[]
index|[]
name|fields
init|=
name|HRegionInfo
operator|.
name|parseRegionName
argument_list|(
name|regionName
argument_list|)
decl_stmt|;
name|assertArrayEquals
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|fields
index|[
literal|0
index|]
argument_list|)
argument_list|,
name|tableName
operator|.
name|getName
argument_list|()
argument_list|,
name|fields
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|fields
index|[
literal|1
index|]
argument_list|)
argument_list|,
name|startKey
argument_list|,
name|fields
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|fields
index|[
literal|2
index|]
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|Long
operator|.
name|toString
argument_list|(
name|regionId
argument_list|)
argument_list|)
argument_list|,
name|fields
index|[
literal|2
index|]
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|fields
operator|.
name|length
argument_list|)
expr_stmt|;
comment|// test with replicaId
name|regionName
operator|=
name|HRegionInfo
operator|.
name|createRegionName
argument_list|(
name|tableName
argument_list|,
name|startKey
argument_list|,
name|regionId
argument_list|,
name|replicaId
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|fields
operator|=
name|HRegionInfo
operator|.
name|parseRegionName
argument_list|(
name|regionName
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|fields
index|[
literal|0
index|]
argument_list|)
argument_list|,
name|tableName
operator|.
name|getName
argument_list|()
argument_list|,
name|fields
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|fields
index|[
literal|1
index|]
argument_list|)
argument_list|,
name|startKey
argument_list|,
name|fields
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|fields
index|[
literal|2
index|]
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|Long
operator|.
name|toString
argument_list|(
name|regionId
argument_list|)
argument_list|)
argument_list|,
name|fields
index|[
literal|2
index|]
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|fields
index|[
literal|3
index|]
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|String
operator|.
name|format
argument_list|(
name|HRegionInfo
operator|.
name|REPLICA_ID_FORMAT
argument_list|,
name|replicaId
argument_list|)
argument_list|)
argument_list|,
name|fields
index|[
literal|3
index|]
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testConvert
parameter_list|()
block|{
specifier|final
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"ns1:"
operator|+
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
decl_stmt|;
name|byte
index|[]
name|startKey
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"startKey"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|endKey
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"endKey"
argument_list|)
decl_stmt|;
name|boolean
name|split
init|=
literal|false
decl_stmt|;
name|long
name|regionId
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|int
name|replicaId
init|=
literal|42
decl_stmt|;
name|HRegionInfo
name|hri
init|=
operator|new
name|HRegionInfo
argument_list|(
name|tableName
argument_list|,
name|startKey
argument_list|,
name|endKey
argument_list|,
name|split
argument_list|,
name|regionId
argument_list|,
name|replicaId
argument_list|)
decl_stmt|;
comment|// convert two times, compare
name|HRegionInfo
name|convertedHri
init|=
name|HRegionInfo
operator|.
name|convert
argument_list|(
name|HRegionInfo
operator|.
name|convert
argument_list|(
name|hri
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|hri
argument_list|,
name|convertedHri
argument_list|)
expr_stmt|;
comment|// test convert RegionInfo without replicaId
name|RegionInfo
name|info
init|=
name|RegionInfo
operator|.
name|newBuilder
argument_list|()
operator|.
name|setTableName
argument_list|(
name|HBaseProtos
operator|.
name|TableName
operator|.
name|newBuilder
argument_list|()
operator|.
name|setQualifier
argument_list|(
name|UnsafeByteOperations
operator|.
name|unsafeWrap
argument_list|(
name|tableName
operator|.
name|getQualifier
argument_list|()
argument_list|)
argument_list|)
operator|.
name|setNamespace
argument_list|(
name|UnsafeByteOperations
operator|.
name|unsafeWrap
argument_list|(
name|tableName
operator|.
name|getNamespace
argument_list|()
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|setStartKey
argument_list|(
name|UnsafeByteOperations
operator|.
name|unsafeWrap
argument_list|(
name|startKey
argument_list|)
argument_list|)
operator|.
name|setEndKey
argument_list|(
name|UnsafeByteOperations
operator|.
name|unsafeWrap
argument_list|(
name|endKey
argument_list|)
argument_list|)
operator|.
name|setSplit
argument_list|(
name|split
argument_list|)
operator|.
name|setRegionId
argument_list|(
name|regionId
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|convertedHri
operator|=
name|HRegionInfo
operator|.
name|convert
argument_list|(
name|info
argument_list|)
expr_stmt|;
name|HRegionInfo
name|expectedHri
init|=
operator|new
name|HRegionInfo
argument_list|(
name|tableName
argument_list|,
name|startKey
argument_list|,
name|endKey
argument_list|,
name|split
argument_list|,
name|regionId
argument_list|,
literal|0
argument_list|)
decl_stmt|;
comment|// expecting default replicaId
name|assertEquals
argument_list|(
name|expectedHri
argument_list|,
name|convertedHri
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRegionDetailsForDisplay
parameter_list|()
throws|throws
name|IOException
block|{
name|byte
index|[]
name|startKey
init|=
operator|new
name|byte
index|[]
block|{
literal|0x01
block|,
literal|0x01
block|,
literal|0x02
block|,
literal|0x03
block|}
decl_stmt|;
name|byte
index|[]
name|endKey
init|=
operator|new
name|byte
index|[]
block|{
literal|0x01
block|,
literal|0x01
block|,
literal|0x02
block|,
literal|0x04
block|}
decl_stmt|;
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
literal|"hbase.display.keys"
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|HRegionInfo
name|h
init|=
operator|new
name|HRegionInfo
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
argument_list|,
name|startKey
argument_list|,
name|endKey
argument_list|)
decl_stmt|;
name|checkEquality
argument_list|(
name|h
argument_list|,
name|conf
argument_list|)
expr_stmt|;
comment|// check HRIs with non-default replicaId
name|h
operator|=
operator|new
name|HRegionInfo
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
argument_list|,
name|startKey
argument_list|,
name|endKey
argument_list|,
literal|false
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|checkEquality
argument_list|(
name|h
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertArrayEquals
argument_list|(
name|HRegionInfo
operator|.
name|HIDDEN_END_KEY
argument_list|,
name|HRegionInfo
operator|.
name|getEndKeyForDisplay
argument_list|(
name|h
argument_list|,
name|conf
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertArrayEquals
argument_list|(
name|HRegionInfo
operator|.
name|HIDDEN_START_KEY
argument_list|,
name|HRegionInfo
operator|.
name|getStartKeyForDisplay
argument_list|(
name|h
argument_list|,
name|conf
argument_list|)
argument_list|)
expr_stmt|;
name|RegionState
name|state
init|=
operator|new
name|RegionState
argument_list|(
name|h
argument_list|,
name|RegionState
operator|.
name|State
operator|.
name|OPEN
argument_list|)
decl_stmt|;
name|String
name|descriptiveNameForDisplay
init|=
name|HRegionInfo
operator|.
name|getDescriptiveNameFromRegionStateForDisplay
argument_list|(
name|state
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|checkDescriptiveNameEquality
argument_list|(
name|descriptiveNameForDisplay
argument_list|,
name|state
operator|.
name|toDescriptiveString
argument_list|()
argument_list|,
name|startKey
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
literal|"hbase.display.keys"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertArrayEquals
argument_list|(
name|endKey
argument_list|,
name|HRegionInfo
operator|.
name|getEndKeyForDisplay
argument_list|(
name|h
argument_list|,
name|conf
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertArrayEquals
argument_list|(
name|startKey
argument_list|,
name|HRegionInfo
operator|.
name|getStartKeyForDisplay
argument_list|(
name|h
argument_list|,
name|conf
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|state
operator|.
name|toDescriptiveString
argument_list|()
argument_list|,
name|HRegionInfo
operator|.
name|getDescriptiveNameFromRegionStateForDisplay
argument_list|(
name|state
argument_list|,
name|conf
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|checkDescriptiveNameEquality
parameter_list|(
name|String
name|descriptiveNameForDisplay
parameter_list|,
name|String
name|origDesc
parameter_list|,
name|byte
index|[]
name|startKey
parameter_list|)
block|{
comment|// except for the "hidden-start-key" substring everything else should exactly match
name|String
name|firstPart
init|=
name|descriptiveNameForDisplay
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|descriptiveNameForDisplay
operator|.
name|indexOf
argument_list|(
operator|new
name|String
argument_list|(
name|HRegionInfo
operator|.
name|HIDDEN_START_KEY
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|secondPart
init|=
name|descriptiveNameForDisplay
operator|.
name|substring
argument_list|(
name|descriptiveNameForDisplay
operator|.
name|indexOf
argument_list|(
operator|new
name|String
argument_list|(
name|HRegionInfo
operator|.
name|HIDDEN_START_KEY
argument_list|)
argument_list|)
operator|+
name|HRegionInfo
operator|.
name|HIDDEN_START_KEY
operator|.
name|length
argument_list|)
decl_stmt|;
name|String
name|firstPartOrig
init|=
name|origDesc
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|origDesc
operator|.
name|indexOf
argument_list|(
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|startKey
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|secondPartOrig
init|=
name|origDesc
operator|.
name|substring
argument_list|(
name|origDesc
operator|.
name|indexOf
argument_list|(
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|startKey
argument_list|)
argument_list|)
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|startKey
argument_list|)
operator|.
name|length
argument_list|()
argument_list|)
decl_stmt|;
assert|assert
operator|(
name|firstPart
operator|.
name|equals
argument_list|(
name|firstPartOrig
argument_list|)
operator|)
assert|;
assert|assert
operator|(
name|secondPart
operator|.
name|equals
argument_list|(
name|secondPartOrig
argument_list|)
operator|)
assert|;
block|}
specifier|private
name|void
name|checkEquality
parameter_list|(
name|HRegionInfo
name|h
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|byte
index|[]
name|modifiedRegionName
init|=
name|HRegionInfo
operator|.
name|getRegionNameForDisplay
argument_list|(
name|h
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|byte
index|[]
index|[]
name|modifiedRegionNameParts
init|=
name|HRegionInfo
operator|.
name|parseRegionName
argument_list|(
name|modifiedRegionName
argument_list|)
decl_stmt|;
name|byte
index|[]
index|[]
name|regionNameParts
init|=
name|HRegionInfo
operator|.
name|parseRegionName
argument_list|(
name|h
operator|.
name|getRegionName
argument_list|()
argument_list|)
decl_stmt|;
comment|//same number of parts
assert|assert
operator|(
name|modifiedRegionNameParts
operator|.
name|length
operator|==
name|regionNameParts
operator|.
name|length
operator|)
assert|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|regionNameParts
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
comment|// all parts should match except for [1] where in the modified one,
comment|// we should have "hidden_start_key"
if|if
condition|(
name|i
operator|!=
literal|1
condition|)
block|{
name|Assert
operator|.
name|assertArrayEquals
argument_list|(
name|regionNameParts
index|[
name|i
index|]
argument_list|,
name|modifiedRegionNameParts
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|Assert
operator|.
name|assertNotEquals
argument_list|(
name|regionNameParts
index|[
name|i
index|]
index|[
literal|0
index|]
argument_list|,
name|modifiedRegionNameParts
index|[
name|i
index|]
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertArrayEquals
argument_list|(
name|modifiedRegionNameParts
index|[
literal|1
index|]
argument_list|,
name|HRegionInfo
operator|.
name|getStartKeyForDisplay
argument_list|(
name|h
argument_list|,
name|conf
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

