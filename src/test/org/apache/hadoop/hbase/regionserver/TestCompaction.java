begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2007 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|dfs
operator|.
name|MiniDFSCluster
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
name|io
operator|.
name|ImmutableBytesWritable
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
name|io
operator|.
name|MapFile
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
name|io
operator|.
name|Text
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
name|HStoreKey
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
name|io
operator|.
name|Cell
import|;
end_import

begin_comment
comment|/**  * Test compactions  */
end_comment

begin_class
specifier|public
class|class
name|TestCompaction
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
name|TestCompaction
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
name|HRegion
name|r
init|=
literal|null
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|COLUMN_FAMILY
init|=
name|COLFAMILY_NAME1
decl_stmt|;
specifier|private
specifier|final
name|Text
name|STARTROW
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Text
name|COLUMN_FAMILY_TEXT
init|=
operator|new
name|Text
argument_list|(
name|COLUMN_FAMILY
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Text
name|COLUMN_FAMILY_TEXT_MINUS_COLON
init|=
operator|new
name|Text
argument_list|(
name|COLUMN_FAMILY
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|COLUMN_FAMILY
operator|.
name|length
argument_list|()
operator|-
literal|1
argument_list|)
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|COMPACTION_THRESHOLD
init|=
name|MAXVERSIONS
decl_stmt|;
specifier|private
name|MiniDFSCluster
name|cluster
decl_stmt|;
comment|/** constructor */
specifier|public
name|TestCompaction
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
name|STARTROW
operator|=
operator|new
name|Text
argument_list|(
name|START_KEY
argument_list|)
expr_stmt|;
comment|// Set cache flush size to 1MB
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.hregion.memcache.flush.size"
argument_list|,
literal|1024
operator|*
literal|1024
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.hregion.memcache.block.multiplier"
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|this
operator|.
name|cluster
operator|=
literal|null
expr_stmt|;
block|}
comment|/** {@inheritDoc} */
annotation|@
name|Override
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|this
operator|.
name|cluster
operator|=
operator|new
name|MiniDFSCluster
argument_list|(
name|conf
argument_list|,
literal|2
argument_list|,
literal|true
argument_list|,
operator|(
name|String
index|[]
operator|)
literal|null
argument_list|)
expr_stmt|;
comment|// Make the hbase rootdir match the minidfs we just span up
name|this
operator|.
name|conf
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|HBASE_DIR
argument_list|,
name|this
operator|.
name|cluster
operator|.
name|getFileSystem
argument_list|()
operator|.
name|getHomeDirectory
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|super
operator|.
name|setUp
argument_list|()
expr_stmt|;
name|HTableDescriptor
name|htd
init|=
name|createTableDescriptor
argument_list|(
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|this
operator|.
name|r
operator|=
name|createNewHRegion
argument_list|(
name|htd
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/** {@inheritDoc} */
annotation|@
name|Override
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|HLog
name|hlog
init|=
name|r
operator|.
name|getLog
argument_list|()
decl_stmt|;
name|this
operator|.
name|r
operator|.
name|close
argument_list|()
expr_stmt|;
name|hlog
operator|.
name|closeAndDelete
argument_list|()
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|cluster
operator|!=
literal|null
condition|)
block|{
name|shutdownDfs
argument_list|(
name|cluster
argument_list|)
expr_stmt|;
block|}
name|super
operator|.
name|tearDown
argument_list|()
expr_stmt|;
block|}
comment|/**    * Run compaction and flushing memcache    * Assert deletes get cleaned up.    * @throws Exception    */
specifier|public
name|void
name|testCompaction
parameter_list|()
throws|throws
name|Exception
block|{
name|createStoreFile
argument_list|(
name|r
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
name|COMPACTION_THRESHOLD
condition|;
name|i
operator|++
control|)
block|{
name|createStoreFile
argument_list|(
name|r
argument_list|)
expr_stmt|;
block|}
comment|// Add more content.  Now there are about 5 versions of each column.
comment|// Default is that there only 3 (MAXVERSIONS) versions allowed per column.
comment|// Assert> 3 and then after compaction, assert that only 3 versions
comment|// available.
name|addContent
argument_list|(
operator|new
name|HRegionIncommon
argument_list|(
name|r
argument_list|)
argument_list|,
name|COLUMN_FAMILY
argument_list|)
expr_stmt|;
name|Cell
index|[]
name|cellValues
init|=
name|r
operator|.
name|get
argument_list|(
name|STARTROW
argument_list|,
name|COLUMN_FAMILY_TEXT
argument_list|,
literal|100
comment|/*Too many*/
argument_list|)
decl_stmt|;
comment|// Assert that I can get> 5 versions (Should be at least 5 in there).
name|assertTrue
argument_list|(
name|cellValues
operator|.
name|length
operator|>=
literal|5
argument_list|)
expr_stmt|;
name|r
operator|.
name|flushcache
argument_list|()
expr_stmt|;
name|r
operator|.
name|compactStores
argument_list|()
expr_stmt|;
comment|// Now assert that there are 4 versions of a record only: thats the
comment|// 3 versions that should be in the compacted store and then the one more
comment|// we added when we flushed. But could be 3 only if the flush happened
comment|// before the compaction started though we tried to have the threads run
comment|// concurrently (On hudson this happens).
name|byte
index|[]
name|secondRowBytes
init|=
name|START_KEY
operator|.
name|getBytes
argument_list|(
name|HConstants
operator|.
name|UTF8_ENCODING
argument_list|)
decl_stmt|;
comment|// Increment the least significant character so we get to next row.
name|secondRowBytes
index|[
name|START_KEY_BYTES
operator|.
name|length
operator|-
literal|1
index|]
operator|++
expr_stmt|;
name|Text
name|secondRow
init|=
operator|new
name|Text
argument_list|(
name|secondRowBytes
argument_list|)
decl_stmt|;
name|cellValues
operator|=
name|r
operator|.
name|get
argument_list|(
name|secondRow
argument_list|,
name|COLUMN_FAMILY_TEXT
argument_list|,
literal|100
comment|/*Too many*/
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Count of "
operator|+
name|secondRow
operator|+
literal|": "
operator|+
name|cellValues
operator|.
name|length
argument_list|)
expr_stmt|;
comment|// Commented out because fails on an hp+ubuntu single-processor w/ 1G and
comment|// "Intel(R) Pentium(R) 4 CPU 3.20GHz" though passes on all local
comment|// machines and even on hudson.  On said machine, its reporting in the
comment|// LOG line above that there are 3 items in row so it should pass the
comment|// below test.
name|assertTrue
argument_list|(
name|cellValues
operator|.
name|length
operator|==
literal|3
operator|||
name|cellValues
operator|.
name|length
operator|==
literal|4
argument_list|)
expr_stmt|;
comment|// Now add deletes to memcache and then flush it.  That will put us over
comment|// the compaction threshold of 3 store files.  Compacting these store files
comment|// should result in a compacted store file that has no references to the
comment|// deleted row.
name|r
operator|.
name|deleteAll
argument_list|(
name|STARTROW
argument_list|,
name|COLUMN_FAMILY_TEXT
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
expr_stmt|;
comment|// Now, before compacting, remove all instances of the first row so can
comment|// verify that it is removed as we compact.
comment|// Assert all delted.
name|assertNull
argument_list|(
name|r
operator|.
name|get
argument_list|(
name|STARTROW
argument_list|,
name|COLUMN_FAMILY_TEXT
argument_list|,
literal|100
comment|/*Too many*/
argument_list|)
argument_list|)
expr_stmt|;
name|r
operator|.
name|flushcache
argument_list|()
expr_stmt|;
name|assertNull
argument_list|(
name|r
operator|.
name|get
argument_list|(
name|STARTROW
argument_list|,
name|COLUMN_FAMILY_TEXT
argument_list|,
literal|100
comment|/*Too many*/
argument_list|)
argument_list|)
expr_stmt|;
comment|// Add a bit of data and flush it so we for sure have the compaction limit
comment|// for store files.  Usually by this time we will have but if compaction
comment|// included the flush that ran 'concurrently', there may be just the
comment|// compacted store and the flush above when we added deletes.  Add more
comment|// content to be certain.
name|createSmallerStoreFile
argument_list|(
name|this
operator|.
name|r
argument_list|)
expr_stmt|;
name|r
operator|.
name|flushcache
argument_list|()
expr_stmt|;
name|r
operator|.
name|compactStores
argument_list|()
expr_stmt|;
comment|// Assert that the first row is still deleted.
name|cellValues
operator|=
name|r
operator|.
name|get
argument_list|(
name|STARTROW
argument_list|,
name|COLUMN_FAMILY_TEXT
argument_list|,
literal|100
comment|/*Too many*/
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|cellValues
argument_list|)
expr_stmt|;
comment|// Assert the store files do not have the first record 'aaa' keys in them.
for|for
control|(
name|MapFile
operator|.
name|Reader
name|reader
range|:
name|this
operator|.
name|r
operator|.
name|stores
operator|.
name|get
argument_list|(
name|COLUMN_FAMILY_TEXT_MINUS_COLON
argument_list|)
operator|.
name|getReaders
argument_list|()
control|)
block|{
name|reader
operator|.
name|reset
argument_list|()
expr_stmt|;
name|HStoreKey
name|key
init|=
operator|new
name|HStoreKey
argument_list|()
decl_stmt|;
name|ImmutableBytesWritable
name|val
init|=
operator|new
name|ImmutableBytesWritable
argument_list|()
decl_stmt|;
while|while
condition|(
name|reader
operator|.
name|next
argument_list|(
name|key
argument_list|,
name|val
argument_list|)
condition|)
block|{
name|assertFalse
argument_list|(
name|key
operator|.
name|getRow
argument_list|()
operator|.
name|equals
argument_list|(
name|STARTROW
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|void
name|createStoreFile
parameter_list|(
specifier|final
name|HRegion
name|region
parameter_list|)
throws|throws
name|IOException
block|{
name|HRegionIncommon
name|loader
init|=
operator|new
name|HRegionIncommon
argument_list|(
name|region
argument_list|)
decl_stmt|;
name|addContent
argument_list|(
name|loader
argument_list|,
name|COLUMN_FAMILY
argument_list|)
expr_stmt|;
name|loader
operator|.
name|flushcache
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|createSmallerStoreFile
parameter_list|(
specifier|final
name|HRegion
name|region
parameter_list|)
throws|throws
name|IOException
block|{
name|HRegionIncommon
name|loader
init|=
operator|new
name|HRegionIncommon
argument_list|(
name|region
argument_list|)
decl_stmt|;
name|addContent
argument_list|(
name|loader
argument_list|,
name|COLUMN_FAMILY
argument_list|,
operator|(
literal|"bbb"
operator|+
name|PUNCTUATION
operator|)
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|loader
operator|.
name|flushcache
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

