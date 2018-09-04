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
name|snapshot
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
name|io
operator|.
name|InterruptedIOException
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
name|Collection
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
name|concurrent
operator|.
name|Callable
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
name|ExecutionException
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
name|Executor
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
name|ExecutorCompletionService
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
name|regionserver
operator|.
name|HRegionFileSystem
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
name|StoreFileInfo
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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
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
name|SnapshotProtos
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
name|SnapshotProtos
operator|.
name|SnapshotRegionManifest
import|;
end_import

begin_comment
comment|/**  * DO NOT USE DIRECTLY. USE {@link SnapshotManifest}.  *  * Snapshot v1 layout format  *  - Each region in the table is represented by a directory with the .hregioninfo file  *      /snapshotName/regionName/.hregioninfo  *  - Each file present in the table is represented by an empty file  *      /snapshotName/regionName/familyName/fileName  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|final
class|class
name|SnapshotManifestV1
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|SnapshotManifestV1
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|DESCRIPTOR_VERSION
init|=
literal|0
decl_stmt|;
specifier|private
name|SnapshotManifestV1
parameter_list|()
block|{   }
specifier|static
class|class
name|ManifestBuilder
implements|implements
name|SnapshotManifest
operator|.
name|RegionVisitor
argument_list|<
name|HRegionFileSystem
argument_list|,
name|Path
argument_list|>
block|{
specifier|private
specifier|final
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|final
name|Path
name|snapshotDir
decl_stmt|;
specifier|private
specifier|final
name|FileSystem
name|rootFs
decl_stmt|;
specifier|private
specifier|final
name|FileSystem
name|workingDirFs
decl_stmt|;
specifier|public
name|ManifestBuilder
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|FileSystem
name|rootFs
parameter_list|,
specifier|final
name|Path
name|snapshotDir
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|snapshotDir
operator|=
name|snapshotDir
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|rootFs
operator|=
name|rootFs
expr_stmt|;
name|this
operator|.
name|workingDirFs
operator|=
name|snapshotDir
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|HRegionFileSystem
name|regionOpen
parameter_list|(
specifier|final
name|RegionInfo
name|regionInfo
parameter_list|)
throws|throws
name|IOException
block|{
name|HRegionFileSystem
name|snapshotRegionFs
init|=
name|HRegionFileSystem
operator|.
name|createRegionOnFileSystem
argument_list|(
name|conf
argument_list|,
name|workingDirFs
argument_list|,
name|snapshotDir
argument_list|,
name|regionInfo
argument_list|)
decl_stmt|;
return|return
name|snapshotRegionFs
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|regionClose
parameter_list|(
specifier|final
name|HRegionFileSystem
name|region
parameter_list|)
block|{     }
annotation|@
name|Override
specifier|public
name|Path
name|familyOpen
parameter_list|(
specifier|final
name|HRegionFileSystem
name|snapshotRegionFs
parameter_list|,
specifier|final
name|byte
index|[]
name|familyName
parameter_list|)
block|{
name|Path
name|familyDir
init|=
name|snapshotRegionFs
operator|.
name|getStoreDir
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|familyName
argument_list|)
argument_list|)
decl_stmt|;
return|return
name|familyDir
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|familyClose
parameter_list|(
specifier|final
name|HRegionFileSystem
name|region
parameter_list|,
specifier|final
name|Path
name|family
parameter_list|)
block|{     }
annotation|@
name|Override
specifier|public
name|void
name|storeFile
parameter_list|(
specifier|final
name|HRegionFileSystem
name|region
parameter_list|,
specifier|final
name|Path
name|familyDir
parameter_list|,
specifier|final
name|StoreFileInfo
name|storeFile
parameter_list|)
throws|throws
name|IOException
block|{
name|Path
name|referenceFile
init|=
operator|new
name|Path
argument_list|(
name|familyDir
argument_list|,
name|storeFile
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|boolean
name|success
init|=
literal|true
decl_stmt|;
if|if
condition|(
name|storeFile
operator|.
name|isReference
argument_list|()
condition|)
block|{
comment|// write the Reference object to the snapshot
name|storeFile
operator|.
name|getReference
argument_list|()
operator|.
name|write
argument_list|(
name|workingDirFs
argument_list|,
name|referenceFile
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// create "reference" to this store file.  It is intentionally an empty file -- all
comment|// necessary information is captured by its fs location and filename.  This allows us to
comment|// only figure out what needs to be done via a single nn operation (instead of having to
comment|// open and read the files as well).
name|success
operator|=
name|workingDirFs
operator|.
name|createNewFile
argument_list|(
name|referenceFile
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|success
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed to create reference file:"
operator|+
name|referenceFile
argument_list|)
throw|;
block|}
block|}
block|}
specifier|static
name|List
argument_list|<
name|SnapshotRegionManifest
argument_list|>
name|loadRegionManifests
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|Executor
name|executor
parameter_list|,
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|Path
name|snapshotDir
parameter_list|,
specifier|final
name|SnapshotDescription
name|desc
parameter_list|)
throws|throws
name|IOException
block|{
name|FileStatus
index|[]
name|regions
init|=
name|FSUtils
operator|.
name|listStatus
argument_list|(
name|fs
argument_list|,
name|snapshotDir
argument_list|,
operator|new
name|FSUtils
operator|.
name|RegionDirFilter
argument_list|(
name|fs
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|regions
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"No regions under directory:"
operator|+
name|snapshotDir
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
specifier|final
name|ExecutorCompletionService
argument_list|<
name|SnapshotRegionManifest
argument_list|>
name|completionService
init|=
operator|new
name|ExecutorCompletionService
argument_list|<>
argument_list|(
name|executor
argument_list|)
decl_stmt|;
for|for
control|(
specifier|final
name|FileStatus
name|region
range|:
name|regions
control|)
block|{
name|completionService
operator|.
name|submit
argument_list|(
operator|new
name|Callable
argument_list|<
name|SnapshotRegionManifest
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|SnapshotRegionManifest
name|call
parameter_list|()
throws|throws
name|IOException
block|{
name|RegionInfo
name|hri
init|=
name|HRegionFileSystem
operator|.
name|loadRegionInfoFileContent
argument_list|(
name|fs
argument_list|,
name|region
operator|.
name|getPath
argument_list|()
argument_list|)
decl_stmt|;
return|return
name|buildManifestFromDisk
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|snapshotDir
argument_list|,
name|hri
argument_list|)
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
name|ArrayList
argument_list|<
name|SnapshotRegionManifest
argument_list|>
name|regionsManifest
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|regions
operator|.
name|length
argument_list|)
decl_stmt|;
try|try
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
name|regions
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|regionsManifest
operator|.
name|add
argument_list|(
name|completionService
operator|.
name|take
argument_list|()
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|InterruptedIOException
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|ExecutionException
name|e
parameter_list|)
block|{
name|IOException
name|ex
init|=
operator|new
name|IOException
argument_list|()
decl_stmt|;
name|ex
operator|.
name|initCause
argument_list|(
name|e
operator|.
name|getCause
argument_list|()
argument_list|)
expr_stmt|;
throw|throw
name|ex
throw|;
block|}
return|return
name|regionsManifest
return|;
block|}
specifier|static
name|void
name|deleteRegionManifest
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|Path
name|snapshotDir
parameter_list|,
specifier|final
name|SnapshotRegionManifest
name|manifest
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|regionName
init|=
name|SnapshotManifest
operator|.
name|getRegionNameFromManifest
argument_list|(
name|manifest
argument_list|)
decl_stmt|;
name|fs
operator|.
name|delete
argument_list|(
operator|new
name|Path
argument_list|(
name|snapshotDir
argument_list|,
name|regionName
argument_list|)
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
specifier|static
name|SnapshotRegionManifest
name|buildManifestFromDisk
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|Path
name|tableDir
parameter_list|,
specifier|final
name|RegionInfo
name|regionInfo
parameter_list|)
throws|throws
name|IOException
block|{
name|HRegionFileSystem
name|regionFs
init|=
name|HRegionFileSystem
operator|.
name|openRegionFromFileSystem
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|tableDir
argument_list|,
name|regionInfo
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|SnapshotRegionManifest
operator|.
name|Builder
name|manifest
init|=
name|SnapshotRegionManifest
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
comment|// 1. dump region meta info into the snapshot directory
name|LOG
operator|.
name|debug
argument_list|(
literal|"Storing region-info for snapshot."
argument_list|)
expr_stmt|;
name|manifest
operator|.
name|setRegionInfo
argument_list|(
name|ProtobufUtil
operator|.
name|toRegionInfo
argument_list|(
name|regionInfo
argument_list|)
argument_list|)
expr_stmt|;
comment|// 2. iterate through all the stores in the region
name|LOG
operator|.
name|debug
argument_list|(
literal|"Creating references for hfiles"
argument_list|)
expr_stmt|;
comment|// This ensures that we have an atomic view of the directory as long as we have< ls limit
comment|// (batch size of the files in a directory) on the namenode. Otherwise, we get back the files in
comment|// batches and may miss files being added/deleted. This could be more robust (iteratively
comment|// checking to see if we have all the files until we are sure), but the limit is currently 1000
comment|// files/batch, far more than the number of store files under a single column family.
name|Collection
argument_list|<
name|String
argument_list|>
name|familyNames
init|=
name|regionFs
operator|.
name|getFamilies
argument_list|()
decl_stmt|;
if|if
condition|(
name|familyNames
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|String
name|familyName
range|:
name|familyNames
control|)
block|{
name|Collection
argument_list|<
name|StoreFileInfo
argument_list|>
name|storeFiles
init|=
name|regionFs
operator|.
name|getStoreFiles
argument_list|(
name|familyName
argument_list|,
literal|false
argument_list|)
decl_stmt|;
if|if
condition|(
name|storeFiles
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"No files under family: "
operator|+
name|familyName
argument_list|)
expr_stmt|;
continue|continue;
block|}
comment|// 2.1. build the snapshot reference for the store
name|SnapshotRegionManifest
operator|.
name|FamilyFiles
operator|.
name|Builder
name|family
init|=
name|SnapshotRegionManifest
operator|.
name|FamilyFiles
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|family
operator|.
name|setFamilyName
argument_list|(
name|UnsafeByteOperations
operator|.
name|unsafeWrap
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|familyName
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Adding snapshot references for "
operator|+
name|storeFiles
operator|+
literal|" hfiles"
argument_list|)
expr_stmt|;
block|}
comment|// 2.2. iterate through all the store's files and create "references".
name|int
name|i
init|=
literal|0
decl_stmt|;
name|int
name|sz
init|=
name|storeFiles
operator|.
name|size
argument_list|()
decl_stmt|;
for|for
control|(
name|StoreFileInfo
name|storeFile
range|:
name|storeFiles
control|)
block|{
comment|// create "reference" to this store file.
name|LOG
operator|.
name|debug
argument_list|(
literal|"Adding reference for file ("
operator|+
operator|(
operator|++
name|i
operator|)
operator|+
literal|"/"
operator|+
name|sz
operator|+
literal|"): "
operator|+
name|storeFile
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
name|SnapshotRegionManifest
operator|.
name|StoreFile
operator|.
name|Builder
name|sfManifest
init|=
name|SnapshotRegionManifest
operator|.
name|StoreFile
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|sfManifest
operator|.
name|setName
argument_list|(
name|storeFile
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|family
operator|.
name|addStoreFiles
argument_list|(
name|sfManifest
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|manifest
operator|.
name|addFamilyFiles
argument_list|(
name|family
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|manifest
operator|.
name|build
argument_list|()
return|;
block|}
block|}
end_class

end_unit

