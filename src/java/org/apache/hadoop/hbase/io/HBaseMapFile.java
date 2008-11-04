begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2008 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|io
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
name|FSDataInputStream
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
name|util
operator|.
name|Writables
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
name|SequenceFile
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
name|Writable
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
name|WritableComparable
import|;
end_import

begin_import
import|import
name|org
operator|.
name|onelab
operator|.
name|filter
operator|.
name|Key
import|;
end_import

begin_comment
comment|/**  * Hbase customizations of MapFile.  */
end_comment

begin_class
specifier|public
class|class
name|HBaseMapFile
extends|extends
name|MapFile
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
name|HBaseMapFile
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Values are instances of this class.    */
specifier|public
specifier|static
specifier|final
name|Class
argument_list|<
name|?
extends|extends
name|Writable
argument_list|>
name|VALUE_CLASS
init|=
name|ImmutableBytesWritable
operator|.
name|class
decl_stmt|;
comment|/**    * A reader capable of reading and caching blocks of the data file.    */
specifier|public
specifier|static
class|class
name|HBaseReader
extends|extends
name|MapFile
operator|.
name|Reader
block|{
specifier|private
specifier|final
name|boolean
name|blockCacheEnabled
decl_stmt|;
comment|/**      * @param fs      * @param dirName      * @param conf      * @param hri      * @throws IOException      */
specifier|public
name|HBaseReader
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|String
name|dirName
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|HRegionInfo
name|hri
parameter_list|)
throws|throws
name|IOException
block|{
name|this
argument_list|(
name|fs
argument_list|,
name|dirName
argument_list|,
name|conf
argument_list|,
literal|false
argument_list|,
name|hri
argument_list|)
expr_stmt|;
block|}
comment|/**      * @param fs      * @param dirName      * @param conf      * @param blockCacheEnabled      * @param hri      * @throws IOException      */
specifier|public
name|HBaseReader
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|String
name|dirName
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|boolean
name|blockCacheEnabled
parameter_list|,
name|HRegionInfo
name|hri
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|fs
argument_list|,
name|dirName
argument_list|,
operator|new
name|HStoreKey
operator|.
name|HStoreKeyWritableComparator
argument_list|(
name|hri
argument_list|)
argument_list|,
name|conf
argument_list|,
literal|false
argument_list|)
expr_stmt|;
comment|// defer opening streams
name|this
operator|.
name|blockCacheEnabled
operator|=
name|blockCacheEnabled
expr_stmt|;
name|open
argument_list|(
name|fs
argument_list|,
name|dirName
argument_list|,
operator|new
name|HStoreKey
operator|.
name|HStoreKeyWritableComparator
argument_list|(
name|hri
argument_list|)
argument_list|,
name|conf
argument_list|)
expr_stmt|;
comment|// Force reading of the mapfile index by calling midKey. Reading the
comment|// index will bring the index into memory over here on the client and
comment|// then close the index file freeing up socket connection and resources
comment|// in the datanode. Usually, the first access on a MapFile.Reader will
comment|// load the index force the issue in HStoreFile MapFiles because an
comment|// access may not happen for some time; meantime we're using up datanode
comment|// resources (See HADOOP-2341). midKey() goes to index. Does not seek.
name|midKey
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|io
operator|.
name|SequenceFile
operator|.
name|Reader
name|createDataFileReader
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|dataFile
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|blockCacheEnabled
condition|)
block|{
return|return
name|super
operator|.
name|createDataFileReader
argument_list|(
name|fs
argument_list|,
name|dataFile
argument_list|,
name|conf
argument_list|)
return|;
block|}
specifier|final
name|int
name|blockSize
init|=
name|conf
operator|.
name|getInt
argument_list|(
literal|"hbase.hstore.blockCache.blockSize"
argument_list|,
literal|64
operator|*
literal|1024
argument_list|)
decl_stmt|;
return|return
operator|new
name|SequenceFile
operator|.
name|Reader
argument_list|(
name|fs
argument_list|,
name|dataFile
argument_list|,
name|conf
argument_list|)
block|{
annotation|@
name|Override
specifier|protected
name|FSDataInputStream
name|openFile
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|file
parameter_list|,
name|int
name|bufferSize
parameter_list|,
name|long
name|length
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|FSDataInputStream
argument_list|(
operator|new
name|BlockFSInputStream
argument_list|(
name|super
operator|.
name|openFile
argument_list|(
name|fs
argument_list|,
name|file
argument_list|,
name|bufferSize
argument_list|,
name|length
argument_list|)
argument_list|,
name|length
argument_list|,
name|blockSize
argument_list|)
argument_list|)
return|;
block|}
block|}
return|;
block|}
block|}
specifier|public
specifier|static
class|class
name|HBaseWriter
extends|extends
name|MapFile
operator|.
name|Writer
block|{
comment|/**      * @param conf      * @param fs      * @param dirName      * @param compression      * @param hri      * @throws IOException      */
specifier|public
name|HBaseWriter
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|String
name|dirName
parameter_list|,
name|SequenceFile
operator|.
name|CompressionType
name|compression
parameter_list|,
specifier|final
name|HRegionInfo
name|hri
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|dirName
argument_list|,
operator|new
name|HStoreKey
operator|.
name|HStoreKeyWritableComparator
argument_list|(
name|hri
argument_list|)
argument_list|,
name|VALUE_CLASS
argument_list|,
name|compression
argument_list|)
expr_stmt|;
comment|// Default for mapfiles is 128.  Makes random reads faster if we
comment|// have more keys indexed and we're not 'next'-ing around in the
comment|// mapfile.
name|setIndexInterval
argument_list|(
name|conf
operator|.
name|getInt
argument_list|(
literal|"hbase.io.index.interval"
argument_list|,
literal|32
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

