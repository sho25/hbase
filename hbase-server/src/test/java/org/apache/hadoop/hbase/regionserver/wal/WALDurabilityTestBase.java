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
name|regionserver
operator|.
name|wal
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
name|assertNull
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
name|FileSystem
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
name|Durability
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
name|regionserver
operator|.
name|ChunkCreator
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
name|HRegion
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
name|MemStoreLABImpl
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
name|wal
operator|.
name|WAL
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|After
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
name|rules
operator|.
name|TestName
import|;
end_import

begin_comment
comment|/**  * Tests for WAL write durability - hflush vs hsync  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|WALDurabilityTestBase
parameter_list|<
name|T
extends|extends
name|WAL
parameter_list|>
block|{
specifier|private
specifier|static
specifier|final
name|String
name|COLUMN_FAMILY
init|=
literal|"MyCF"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|COLUMN_FAMILY_BYTES
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|COLUMN_FAMILY
argument_list|)
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
name|Configuration
name|conf
decl_stmt|;
specifier|private
name|String
name|dir
decl_stmt|;
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
comment|// Test names
specifier|protected
name|TableName
name|tableName
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|IOException
block|{
name|conf
operator|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
name|dir
operator|=
name|TEST_UTIL
operator|.
name|getDataTestDir
argument_list|(
literal|"TestHRegion"
argument_list|)
operator|.
name|toString
argument_list|()
expr_stmt|;
name|tableName
operator|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|IOException
block|{
name|TEST_UTIL
operator|.
name|cleanupTestDir
argument_list|()
expr_stmt|;
block|}
specifier|protected
specifier|abstract
name|T
name|getWAL
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|root
parameter_list|,
name|String
name|logDir
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
function_decl|;
specifier|protected
specifier|abstract
name|void
name|resetSyncFlag
parameter_list|(
name|T
name|wal
parameter_list|)
function_decl|;
specifier|protected
specifier|abstract
name|Boolean
name|getSyncFlag
parameter_list|(
name|T
name|wal
parameter_list|)
function_decl|;
specifier|protected
specifier|abstract
name|Boolean
name|getWriterSyncFlag
parameter_list|(
name|T
name|wal
parameter_list|)
function_decl|;
annotation|@
name|Test
specifier|public
name|void
name|testWALDurability
parameter_list|()
throws|throws
name|IOException
block|{
name|byte
index|[]
name|bytes
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|bytes
argument_list|)
decl_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|COLUMN_FAMILY_BYTES
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"1"
argument_list|)
argument_list|,
name|bytes
argument_list|)
expr_stmt|;
comment|// global hbase.wal.hsync false, no override in put call - hflush
name|conf
operator|.
name|set
argument_list|(
name|HRegion
operator|.
name|WAL_HSYNC_CONF_KEY
argument_list|,
literal|"false"
argument_list|)
expr_stmt|;
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|Path
name|rootDir
init|=
operator|new
name|Path
argument_list|(
name|dir
operator|+
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|T
name|wal
init|=
name|getWAL
argument_list|(
name|fs
argument_list|,
name|rootDir
argument_list|,
name|getName
argument_list|()
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|HRegion
name|region
init|=
name|initHRegion
argument_list|(
name|tableName
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|wal
argument_list|)
decl_stmt|;
try|try
block|{
name|resetSyncFlag
argument_list|(
name|wal
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|getSyncFlag
argument_list|(
name|wal
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|getWriterSyncFlag
argument_list|(
name|wal
argument_list|)
argument_list|)
expr_stmt|;
name|region
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|getSyncFlag
argument_list|(
name|wal
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|getWriterSyncFlag
argument_list|(
name|wal
argument_list|)
argument_list|)
expr_stmt|;
comment|// global hbase.wal.hsync false, durability set in put call - fsync
name|put
operator|.
name|setDurability
argument_list|(
name|Durability
operator|.
name|FSYNC_WAL
argument_list|)
expr_stmt|;
name|resetSyncFlag
argument_list|(
name|wal
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|getSyncFlag
argument_list|(
name|wal
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|getWriterSyncFlag
argument_list|(
name|wal
argument_list|)
argument_list|)
expr_stmt|;
name|region
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|getSyncFlag
argument_list|(
name|wal
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|getWriterSyncFlag
argument_list|(
name|wal
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|HBaseTestingUtility
operator|.
name|closeRegionAndWAL
argument_list|(
name|region
argument_list|)
expr_stmt|;
block|}
comment|// global hbase.wal.hsync true, no override in put call
name|conf
operator|.
name|set
argument_list|(
name|HRegion
operator|.
name|WAL_HSYNC_CONF_KEY
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
name|fs
operator|=
name|FileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|wal
operator|=
name|getWAL
argument_list|(
name|fs
argument_list|,
name|rootDir
argument_list|,
name|getName
argument_list|()
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|region
operator|=
name|initHRegion
argument_list|(
name|tableName
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|wal
argument_list|)
expr_stmt|;
try|try
block|{
name|resetSyncFlag
argument_list|(
name|wal
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|getSyncFlag
argument_list|(
name|wal
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|getWriterSyncFlag
argument_list|(
name|wal
argument_list|)
argument_list|)
expr_stmt|;
name|region
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|getSyncFlag
argument_list|(
name|wal
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|getWriterSyncFlag
argument_list|(
name|wal
argument_list|)
argument_list|)
expr_stmt|;
comment|// global hbase.wal.hsync true, durability set in put call - fsync
name|put
operator|.
name|setDurability
argument_list|(
name|Durability
operator|.
name|FSYNC_WAL
argument_list|)
expr_stmt|;
name|resetSyncFlag
argument_list|(
name|wal
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|getSyncFlag
argument_list|(
name|wal
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|getWriterSyncFlag
argument_list|(
name|wal
argument_list|)
argument_list|)
expr_stmt|;
name|region
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|getSyncFlag
argument_list|(
name|wal
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|getWriterSyncFlag
argument_list|(
name|wal
argument_list|)
argument_list|)
expr_stmt|;
comment|// global hbase.wal.hsync true, durability set in put call - sync
name|put
operator|=
operator|new
name|Put
argument_list|(
name|bytes
argument_list|)
expr_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|COLUMN_FAMILY_BYTES
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"1"
argument_list|)
argument_list|,
name|bytes
argument_list|)
expr_stmt|;
name|put
operator|.
name|setDurability
argument_list|(
name|Durability
operator|.
name|SYNC_WAL
argument_list|)
expr_stmt|;
name|resetSyncFlag
argument_list|(
name|wal
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|getSyncFlag
argument_list|(
name|wal
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|getWriterSyncFlag
argument_list|(
name|wal
argument_list|)
argument_list|)
expr_stmt|;
name|region
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|getSyncFlag
argument_list|(
name|wal
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|getWriterSyncFlag
argument_list|(
name|wal
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|HBaseTestingUtility
operator|.
name|closeRegionAndWAL
argument_list|(
name|region
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|String
name|getName
parameter_list|()
block|{
return|return
name|name
operator|.
name|getMethodName
argument_list|()
return|;
block|}
comment|/**    * @return A region on which you must call {@link HBaseTestingUtility#closeRegionAndWAL(HRegion)}    *         when done.    */
specifier|public
specifier|static
name|HRegion
name|initHRegion
parameter_list|(
name|TableName
name|tableName
parameter_list|,
name|byte
index|[]
name|startKey
parameter_list|,
name|byte
index|[]
name|stopKey
parameter_list|,
name|WAL
name|wal
parameter_list|)
throws|throws
name|IOException
block|{
name|ChunkCreator
operator|.
name|initialize
argument_list|(
name|MemStoreLABImpl
operator|.
name|CHUNK_SIZE_DEFAULT
argument_list|,
literal|false
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|null
argument_list|)
expr_stmt|;
return|return
name|TEST_UTIL
operator|.
name|createLocalHRegion
argument_list|(
name|tableName
argument_list|,
name|startKey
argument_list|,
name|stopKey
argument_list|,
literal|false
argument_list|,
name|Durability
operator|.
name|USE_DEFAULT
argument_list|,
name|wal
argument_list|,
name|COLUMN_FAMILY_BYTES
argument_list|)
return|;
block|}
block|}
end_class

end_unit

