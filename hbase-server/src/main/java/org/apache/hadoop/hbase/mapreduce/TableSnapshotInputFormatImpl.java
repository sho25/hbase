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
name|mapreduce
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
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
name|CellUtil
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
name|HDFSBlocksDistribution
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
name|HDFSBlocksDistribution
operator|.
name|HostAndWeight
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
name|classification
operator|.
name|InterfaceAudience
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
name|classification
operator|.
name|InterfaceStability
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
name|ClientSideRegionScanner
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
name|IsolationLevel
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
name|Result
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
name|shaded
operator|.
name|protobuf
operator|.
name|ProtobufUtil
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
name|SnapshotDescription
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
name|MapReduceProtos
operator|.
name|TableSnapshotRegionSplit
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
name|SnapshotProtos
operator|.
name|SnapshotRegionManifest
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
name|snapshot
operator|.
name|RestoreSnapshotHelper
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
name|snapshot
operator|.
name|SnapshotDescriptionUtils
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
name|snapshot
operator|.
name|SnapshotManifest
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
name|FSUtils
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
name|java
operator|.
name|io
operator|.
name|ByteArrayOutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutput
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
name|ArrayList
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
name|UUID
import|;
end_import

begin_comment
comment|/**  * Hadoop MR API-agnostic implementation for mapreduce over table snapshots.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
class|class
name|TableSnapshotInputFormatImpl
block|{
comment|// TODO: Snapshots files are owned in fs by the hbase user. There is no
comment|// easy way to delegate access.
specifier|public
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TableSnapshotInputFormatImpl
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|SNAPSHOT_NAME_KEY
init|=
literal|"hbase.TableSnapshotInputFormat.snapshot.name"
decl_stmt|;
comment|// key for specifying the root dir of the restored snapshot
specifier|protected
specifier|static
specifier|final
name|String
name|RESTORE_DIR_KEY
init|=
literal|"hbase.TableSnapshotInputFormat.restore.dir"
decl_stmt|;
comment|/** See {@link #getBestLocations(Configuration, HDFSBlocksDistribution)} */
specifier|private
specifier|static
specifier|final
name|String
name|LOCALITY_CUTOFF_MULTIPLIER
init|=
literal|"hbase.tablesnapshotinputformat.locality.cutoff.multiplier"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|float
name|DEFAULT_LOCALITY_CUTOFF_MULTIPLIER
init|=
literal|0.8f
decl_stmt|;
comment|/**    * Implementation class for InputSplit logic common between mapred and mapreduce.    */
specifier|public
specifier|static
class|class
name|InputSplit
implements|implements
name|Writable
block|{
specifier|private
name|HTableDescriptor
name|htd
decl_stmt|;
specifier|private
name|HRegionInfo
name|regionInfo
decl_stmt|;
specifier|private
name|String
index|[]
name|locations
decl_stmt|;
specifier|private
name|String
name|scan
decl_stmt|;
specifier|private
name|String
name|restoreDir
decl_stmt|;
comment|// constructor for mapreduce framework / Writable
specifier|public
name|InputSplit
parameter_list|()
block|{}
specifier|public
name|InputSplit
parameter_list|(
name|HTableDescriptor
name|htd
parameter_list|,
name|HRegionInfo
name|regionInfo
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|locations
parameter_list|,
name|Scan
name|scan
parameter_list|,
name|Path
name|restoreDir
parameter_list|)
block|{
name|this
operator|.
name|htd
operator|=
name|htd
expr_stmt|;
name|this
operator|.
name|regionInfo
operator|=
name|regionInfo
expr_stmt|;
if|if
condition|(
name|locations
operator|==
literal|null
operator|||
name|locations
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|this
operator|.
name|locations
operator|=
operator|new
name|String
index|[
literal|0
index|]
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|locations
operator|=
name|locations
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
name|locations
operator|.
name|size
argument_list|()
index|]
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|this
operator|.
name|scan
operator|=
name|scan
operator|!=
literal|null
condition|?
name|TableMapReduceUtil
operator|.
name|convertScanToString
argument_list|(
name|scan
argument_list|)
else|:
literal|""
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed to convert Scan to String"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|restoreDir
operator|=
name|restoreDir
operator|.
name|toString
argument_list|()
expr_stmt|;
block|}
specifier|public
name|HTableDescriptor
name|getHtd
parameter_list|()
block|{
return|return
name|htd
return|;
block|}
specifier|public
name|String
name|getScan
parameter_list|()
block|{
return|return
name|scan
return|;
block|}
specifier|public
name|String
name|getRestoreDir
parameter_list|()
block|{
return|return
name|restoreDir
return|;
block|}
specifier|public
name|long
name|getLength
parameter_list|()
block|{
comment|//TODO: We can obtain the file sizes of the snapshot here.
return|return
literal|0
return|;
block|}
specifier|public
name|String
index|[]
name|getLocations
parameter_list|()
block|{
return|return
name|locations
return|;
block|}
specifier|public
name|HTableDescriptor
name|getTableDescriptor
parameter_list|()
block|{
return|return
name|htd
return|;
block|}
specifier|public
name|HRegionInfo
name|getRegionInfo
parameter_list|()
block|{
return|return
name|regionInfo
return|;
block|}
comment|// TODO: We should have ProtobufSerialization in Hadoop, and directly use PB objects instead of
comment|// doing this wrapping with Writables.
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|DataOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|TableSnapshotRegionSplit
operator|.
name|Builder
name|builder
init|=
name|TableSnapshotRegionSplit
operator|.
name|newBuilder
argument_list|()
operator|.
name|setTable
argument_list|(
name|ProtobufUtil
operator|.
name|convertToTableSchema
argument_list|(
name|htd
argument_list|)
argument_list|)
operator|.
name|setRegion
argument_list|(
name|HRegionInfo
operator|.
name|convert
argument_list|(
name|regionInfo
argument_list|)
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|location
range|:
name|locations
control|)
block|{
name|builder
operator|.
name|addLocations
argument_list|(
name|location
argument_list|)
expr_stmt|;
block|}
name|TableSnapshotRegionSplit
name|split
init|=
name|builder
operator|.
name|build
argument_list|()
decl_stmt|;
name|ByteArrayOutputStream
name|baos
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|split
operator|.
name|writeTo
argument_list|(
name|baos
argument_list|)
expr_stmt|;
name|baos
operator|.
name|close
argument_list|()
expr_stmt|;
name|byte
index|[]
name|buf
init|=
name|baos
operator|.
name|toByteArray
argument_list|()
decl_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|buf
operator|.
name|length
argument_list|)
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
name|buf
argument_list|)
expr_stmt|;
name|Bytes
operator|.
name|writeByteArray
argument_list|(
name|out
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|scan
argument_list|)
argument_list|)
expr_stmt|;
name|Bytes
operator|.
name|writeByteArray
argument_list|(
name|out
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|restoreDir
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|readFields
parameter_list|(
name|DataInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|len
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
name|byte
index|[]
name|buf
init|=
operator|new
name|byte
index|[
name|len
index|]
decl_stmt|;
name|in
operator|.
name|readFully
argument_list|(
name|buf
argument_list|)
expr_stmt|;
name|TableSnapshotRegionSplit
name|split
init|=
name|TableSnapshotRegionSplit
operator|.
name|PARSER
operator|.
name|parseFrom
argument_list|(
name|buf
argument_list|)
decl_stmt|;
name|this
operator|.
name|htd
operator|=
name|ProtobufUtil
operator|.
name|convertToHTableDesc
argument_list|(
name|split
operator|.
name|getTable
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|regionInfo
operator|=
name|HRegionInfo
operator|.
name|convert
argument_list|(
name|split
operator|.
name|getRegion
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|locationsList
init|=
name|split
operator|.
name|getLocationsList
argument_list|()
decl_stmt|;
name|this
operator|.
name|locations
operator|=
name|locationsList
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
name|locationsList
operator|.
name|size
argument_list|()
index|]
argument_list|)
expr_stmt|;
name|this
operator|.
name|scan
operator|=
name|Bytes
operator|.
name|toString
argument_list|(
name|Bytes
operator|.
name|readByteArray
argument_list|(
name|in
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|restoreDir
operator|=
name|Bytes
operator|.
name|toString
argument_list|(
name|Bytes
operator|.
name|readByteArray
argument_list|(
name|in
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Implementation class for RecordReader logic common between mapred and mapreduce.    */
specifier|public
specifier|static
class|class
name|RecordReader
block|{
specifier|private
name|InputSplit
name|split
decl_stmt|;
specifier|private
name|Scan
name|scan
decl_stmt|;
specifier|private
name|Result
name|result
init|=
literal|null
decl_stmt|;
specifier|private
name|ImmutableBytesWritable
name|row
init|=
literal|null
decl_stmt|;
specifier|private
name|ClientSideRegionScanner
name|scanner
decl_stmt|;
specifier|public
name|ClientSideRegionScanner
name|getScanner
parameter_list|()
block|{
return|return
name|scanner
return|;
block|}
specifier|public
name|void
name|initialize
parameter_list|(
name|InputSplit
name|split
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|scan
operator|=
name|TableMapReduceUtil
operator|.
name|convertStringToScan
argument_list|(
name|split
operator|.
name|getScan
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|split
operator|=
name|split
expr_stmt|;
name|HTableDescriptor
name|htd
init|=
name|split
operator|.
name|htd
decl_stmt|;
name|HRegionInfo
name|hri
init|=
name|this
operator|.
name|split
operator|.
name|getRegionInfo
argument_list|()
decl_stmt|;
name|FileSystem
name|fs
init|=
name|FSUtils
operator|.
name|getCurrentFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
comment|// region is immutable, this should be fine,
comment|// otherwise we have to set the thread read point
name|scan
operator|.
name|setIsolationLevel
argument_list|(
name|IsolationLevel
operator|.
name|READ_UNCOMMITTED
argument_list|)
expr_stmt|;
comment|// disable caching of data blocks
name|scan
operator|.
name|setCacheBlocks
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|scanner
operator|=
operator|new
name|ClientSideRegionScanner
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
operator|new
name|Path
argument_list|(
name|split
operator|.
name|restoreDir
argument_list|)
argument_list|,
name|htd
argument_list|,
name|hri
argument_list|,
name|scan
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|public
name|boolean
name|nextKeyValue
parameter_list|()
throws|throws
name|IOException
block|{
name|result
operator|=
name|scanner
operator|.
name|next
argument_list|()
expr_stmt|;
if|if
condition|(
name|result
operator|==
literal|null
condition|)
block|{
comment|//we are done
return|return
literal|false
return|;
block|}
if|if
condition|(
name|this
operator|.
name|row
operator|==
literal|null
condition|)
block|{
name|this
operator|.
name|row
operator|=
operator|new
name|ImmutableBytesWritable
argument_list|()
expr_stmt|;
block|}
name|this
operator|.
name|row
operator|.
name|set
argument_list|(
name|result
operator|.
name|getRow
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
specifier|public
name|ImmutableBytesWritable
name|getCurrentKey
parameter_list|()
block|{
return|return
name|row
return|;
block|}
specifier|public
name|Result
name|getCurrentValue
parameter_list|()
block|{
return|return
name|result
return|;
block|}
specifier|public
name|long
name|getPos
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
specifier|public
name|float
name|getProgress
parameter_list|()
block|{
return|return
literal|0
return|;
comment|// TODO: use total bytes to estimate
block|}
specifier|public
name|void
name|close
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|scanner
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|scanner
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
specifier|public
specifier|static
name|List
argument_list|<
name|InputSplit
argument_list|>
name|getSplits
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|snapshotName
init|=
name|getSnapshotName
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|Path
name|rootDir
init|=
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
init|=
name|rootDir
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|SnapshotManifest
name|manifest
init|=
name|getSnapshotManifest
argument_list|(
name|conf
argument_list|,
name|snapshotName
argument_list|,
name|rootDir
argument_list|,
name|fs
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regionInfos
init|=
name|getRegionInfosFromManifest
argument_list|(
name|manifest
argument_list|)
decl_stmt|;
comment|// TODO: mapred does not support scan as input API. Work around for now.
name|Scan
name|scan
init|=
name|extractScanFromConf
argument_list|(
name|conf
argument_list|)
decl_stmt|;
comment|// the temp dir where the snapshot is restored
name|Path
name|restoreDir
init|=
operator|new
name|Path
argument_list|(
name|conf
operator|.
name|get
argument_list|(
name|RESTORE_DIR_KEY
argument_list|)
argument_list|)
decl_stmt|;
return|return
name|getSplits
argument_list|(
name|scan
argument_list|,
name|manifest
argument_list|,
name|regionInfos
argument_list|,
name|restoreDir
argument_list|,
name|conf
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|getRegionInfosFromManifest
parameter_list|(
name|SnapshotManifest
name|manifest
parameter_list|)
block|{
name|List
argument_list|<
name|SnapshotRegionManifest
argument_list|>
name|regionManifests
init|=
name|manifest
operator|.
name|getRegionManifests
argument_list|()
decl_stmt|;
if|if
condition|(
name|regionManifests
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Snapshot seems empty"
argument_list|)
throw|;
block|}
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regionInfos
init|=
name|Lists
operator|.
name|newArrayListWithCapacity
argument_list|(
name|regionManifests
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|SnapshotRegionManifest
name|regionManifest
range|:
name|regionManifests
control|)
block|{
name|regionInfos
operator|.
name|add
argument_list|(
name|HRegionInfo
operator|.
name|convert
argument_list|(
name|regionManifest
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|regionInfos
return|;
block|}
specifier|public
specifier|static
name|SnapshotManifest
name|getSnapshotManifest
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|snapshotName
parameter_list|,
name|Path
name|rootDir
parameter_list|,
name|FileSystem
name|fs
parameter_list|)
throws|throws
name|IOException
block|{
name|Path
name|snapshotDir
init|=
name|SnapshotDescriptionUtils
operator|.
name|getCompletedSnapshotDir
argument_list|(
name|snapshotName
argument_list|,
name|rootDir
argument_list|)
decl_stmt|;
name|SnapshotDescription
name|snapshotDesc
init|=
name|SnapshotDescriptionUtils
operator|.
name|readSnapshotInfo
argument_list|(
name|fs
argument_list|,
name|snapshotDir
argument_list|)
decl_stmt|;
return|return
name|SnapshotManifest
operator|.
name|open
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|snapshotDir
argument_list|,
name|snapshotDesc
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|Scan
name|extractScanFromConf
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|Scan
name|scan
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|conf
operator|.
name|get
argument_list|(
name|TableInputFormat
operator|.
name|SCAN
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|scan
operator|=
name|TableMapReduceUtil
operator|.
name|convertStringToScan
argument_list|(
name|conf
operator|.
name|get
argument_list|(
name|TableInputFormat
operator|.
name|SCAN
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|conf
operator|.
name|get
argument_list|(
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|mapred
operator|.
name|TableInputFormat
operator|.
name|COLUMN_LIST
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|String
index|[]
name|columns
init|=
name|conf
operator|.
name|get
argument_list|(
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|mapred
operator|.
name|TableInputFormat
operator|.
name|COLUMN_LIST
argument_list|)
operator|.
name|split
argument_list|(
literal|" "
argument_list|)
decl_stmt|;
name|scan
operator|=
operator|new
name|Scan
argument_list|()
expr_stmt|;
for|for
control|(
name|String
name|col
range|:
name|columns
control|)
block|{
name|scan
operator|.
name|addFamily
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|col
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Unable to create scan"
argument_list|)
throw|;
block|}
return|return
name|scan
return|;
block|}
specifier|public
specifier|static
name|List
argument_list|<
name|InputSplit
argument_list|>
name|getSplits
parameter_list|(
name|Scan
name|scan
parameter_list|,
name|SnapshotManifest
name|manifest
parameter_list|,
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regionManifests
parameter_list|,
name|Path
name|restoreDir
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
comment|// load table descriptor
name|HTableDescriptor
name|htd
init|=
name|manifest
operator|.
name|getTableDescriptor
argument_list|()
decl_stmt|;
name|Path
name|tableDir
init|=
name|FSUtils
operator|.
name|getTableDir
argument_list|(
name|restoreDir
argument_list|,
name|htd
operator|.
name|getTableName
argument_list|()
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|InputSplit
argument_list|>
name|splits
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|HRegionInfo
name|hri
range|:
name|regionManifests
control|)
block|{
comment|// load region descriptor
if|if
condition|(
name|CellUtil
operator|.
name|overlappingKeys
argument_list|(
name|scan
operator|.
name|getStartRow
argument_list|()
argument_list|,
name|scan
operator|.
name|getStopRow
argument_list|()
argument_list|,
name|hri
operator|.
name|getStartKey
argument_list|()
argument_list|,
name|hri
operator|.
name|getEndKey
argument_list|()
argument_list|)
condition|)
block|{
comment|// compute HDFS locations from snapshot files (which will get the locations for
comment|// referred hfiles)
name|List
argument_list|<
name|String
argument_list|>
name|hosts
init|=
name|getBestLocations
argument_list|(
name|conf
argument_list|,
name|HRegion
operator|.
name|computeHDFSBlocksDistribution
argument_list|(
name|conf
argument_list|,
name|htd
argument_list|,
name|hri
argument_list|,
name|tableDir
argument_list|)
argument_list|)
decl_stmt|;
name|int
name|len
init|=
name|Math
operator|.
name|min
argument_list|(
literal|3
argument_list|,
name|hosts
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
name|hosts
operator|=
name|hosts
operator|.
name|subList
argument_list|(
literal|0
argument_list|,
name|len
argument_list|)
expr_stmt|;
name|splits
operator|.
name|add
argument_list|(
operator|new
name|InputSplit
argument_list|(
name|htd
argument_list|,
name|hri
argument_list|,
name|hosts
argument_list|,
name|scan
argument_list|,
name|restoreDir
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|splits
return|;
block|}
comment|/**    * This computes the locations to be passed from the InputSplit. MR/Yarn schedulers does not take    * weights into account, thus will treat every location passed from the input split as equal. We    * do not want to blindly pass all the locations, since we are creating one split per region, and    * the region's blocks are all distributed throughout the cluster unless favorite node assignment    * is used. On the expected stable case, only one location will contain most of the blocks as    * local.    * On the other hand, in favored node assignment, 3 nodes will contain highly local blocks. Here    * we are doing a simple heuristic, where we will pass all hosts which have at least 80%    * (hbase.tablesnapshotinputformat.locality.cutoff.multiplier) as much block locality as the top    * host with the best locality.    */
specifier|public
specifier|static
name|List
argument_list|<
name|String
argument_list|>
name|getBestLocations
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|HDFSBlocksDistribution
name|blockDistribution
parameter_list|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|locations
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
literal|3
argument_list|)
decl_stmt|;
name|HostAndWeight
index|[]
name|hostAndWeights
init|=
name|blockDistribution
operator|.
name|getTopHostsWithWeights
argument_list|()
decl_stmt|;
if|if
condition|(
name|hostAndWeights
operator|.
name|length
operator|==
literal|0
condition|)
block|{
return|return
name|locations
return|;
block|}
name|HostAndWeight
name|topHost
init|=
name|hostAndWeights
index|[
literal|0
index|]
decl_stmt|;
name|locations
operator|.
name|add
argument_list|(
name|topHost
operator|.
name|getHost
argument_list|()
argument_list|)
expr_stmt|;
comment|// Heuristic: filter all hosts which have at least cutoffMultiplier % of block locality
name|double
name|cutoffMultiplier
init|=
name|conf
operator|.
name|getFloat
argument_list|(
name|LOCALITY_CUTOFF_MULTIPLIER
argument_list|,
name|DEFAULT_LOCALITY_CUTOFF_MULTIPLIER
argument_list|)
decl_stmt|;
name|double
name|filterWeight
init|=
name|topHost
operator|.
name|getWeight
argument_list|()
operator|*
name|cutoffMultiplier
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|hostAndWeights
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|hostAndWeights
index|[
name|i
index|]
operator|.
name|getWeight
argument_list|()
operator|>=
name|filterWeight
condition|)
block|{
name|locations
operator|.
name|add
argument_list|(
name|hostAndWeights
index|[
name|i
index|]
operator|.
name|getHost
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
break|break;
block|}
block|}
return|return
name|locations
return|;
block|}
specifier|private
specifier|static
name|String
name|getSnapshotName
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|String
name|snapshotName
init|=
name|conf
operator|.
name|get
argument_list|(
name|SNAPSHOT_NAME_KEY
argument_list|)
decl_stmt|;
if|if
condition|(
name|snapshotName
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Snapshot name must be provided"
argument_list|)
throw|;
block|}
return|return
name|snapshotName
return|;
block|}
comment|/**    * Configures the job to use TableSnapshotInputFormat to read from a snapshot.    * @param conf the job to configuration    * @param snapshotName the name of the snapshot to read from    * @param restoreDir a temporary directory to restore the snapshot into. Current user should    * have write permissions to this directory, and this should not be a subdirectory of rootdir.    * After the job is finished, restoreDir can be deleted.    * @throws IOException if an error occurs    */
specifier|public
specifier|static
name|void
name|setInput
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|snapshotName
parameter_list|,
name|Path
name|restoreDir
parameter_list|)
throws|throws
name|IOException
block|{
name|conf
operator|.
name|set
argument_list|(
name|SNAPSHOT_NAME_KEY
argument_list|,
name|snapshotName
argument_list|)
expr_stmt|;
name|Path
name|rootDir
init|=
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
init|=
name|rootDir
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|restoreDir
operator|=
operator|new
name|Path
argument_list|(
name|restoreDir
argument_list|,
name|UUID
operator|.
name|randomUUID
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
comment|// TODO: restore from record readers to parallelize.
name|RestoreSnapshotHelper
operator|.
name|copySnapshotForScanner
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|rootDir
argument_list|,
name|restoreDir
argument_list|,
name|snapshotName
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|RESTORE_DIR_KEY
argument_list|,
name|restoreDir
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

