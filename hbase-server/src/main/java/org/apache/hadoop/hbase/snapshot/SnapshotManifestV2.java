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
name|ExecutorCompletionService
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
name|FSDataOutputStream
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
name|fs
operator|.
name|PathFilter
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
name|ByteStringer
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

begin_comment
comment|/**  * DO NOT USE DIRECTLY. USE {@link SnapshotManifest}.  *  * Snapshot v2 layout format  *  - Single Manifest file containing all the information of regions  *  - In the online-snapshot case each region will write a "region manifest"  *      /snapshotName/manifest.regionName  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|SnapshotManifestV2
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
name|SnapshotManifestV2
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
literal|2
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|SNAPSHOT_MANIFEST_PREFIX
init|=
literal|"region-manifest."
decl_stmt|;
specifier|static
class|class
name|ManifestBuilder
implements|implements
name|SnapshotManifest
operator|.
name|RegionVisitor
argument_list|<
name|SnapshotRegionManifest
operator|.
name|Builder
argument_list|,
name|SnapshotRegionManifest
operator|.
name|FamilyFiles
operator|.
name|Builder
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
name|fs
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
name|fs
parameter_list|,
specifier|final
name|Path
name|snapshotDir
parameter_list|)
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
name|fs
operator|=
name|fs
expr_stmt|;
block|}
specifier|public
name|SnapshotRegionManifest
operator|.
name|Builder
name|regionOpen
parameter_list|(
specifier|final
name|HRegionInfo
name|regionInfo
parameter_list|)
block|{
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
name|manifest
operator|.
name|setRegionInfo
argument_list|(
name|HRegionInfo
operator|.
name|convert
argument_list|(
name|regionInfo
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|manifest
return|;
block|}
specifier|public
name|void
name|regionClose
parameter_list|(
specifier|final
name|SnapshotRegionManifest
operator|.
name|Builder
name|region
parameter_list|)
throws|throws
name|IOException
block|{
name|SnapshotRegionManifest
name|manifest
init|=
name|region
operator|.
name|build
argument_list|()
decl_stmt|;
name|FSDataOutputStream
name|stream
init|=
name|fs
operator|.
name|create
argument_list|(
name|getRegionManifestPath
argument_list|(
name|snapshotDir
argument_list|,
name|manifest
argument_list|)
argument_list|)
decl_stmt|;
try|try
block|{
name|manifest
operator|.
name|writeTo
argument_list|(
name|stream
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|stream
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
name|SnapshotRegionManifest
operator|.
name|FamilyFiles
operator|.
name|Builder
name|familyOpen
parameter_list|(
specifier|final
name|SnapshotRegionManifest
operator|.
name|Builder
name|region
parameter_list|,
specifier|final
name|byte
index|[]
name|familyName
parameter_list|)
block|{
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
name|ByteStringer
operator|.
name|wrap
argument_list|(
name|familyName
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|family
return|;
block|}
specifier|public
name|void
name|familyClose
parameter_list|(
specifier|final
name|SnapshotRegionManifest
operator|.
name|Builder
name|region
parameter_list|,
specifier|final
name|SnapshotRegionManifest
operator|.
name|FamilyFiles
operator|.
name|Builder
name|family
parameter_list|)
block|{
name|region
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
specifier|public
name|void
name|storeFile
parameter_list|(
specifier|final
name|SnapshotRegionManifest
operator|.
name|Builder
name|region
parameter_list|,
specifier|final
name|SnapshotRegionManifest
operator|.
name|FamilyFiles
operator|.
name|Builder
name|family
parameter_list|,
specifier|final
name|StoreFileInfo
name|storeFile
parameter_list|)
throws|throws
name|IOException
block|{
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
if|if
condition|(
name|storeFile
operator|.
name|isReference
argument_list|()
condition|)
block|{
name|sfManifest
operator|.
name|setReference
argument_list|(
name|storeFile
operator|.
name|getReference
argument_list|()
operator|.
name|convert
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|sfManifest
operator|.
name|setFileSize
argument_list|(
name|storeFile
operator|.
name|getReferencedFileStatus
argument_list|(
name|fs
argument_list|)
operator|.
name|getLen
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
name|manifestFiles
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
name|PathFilter
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|accept
parameter_list|(
name|Path
name|path
parameter_list|)
block|{
return|return
name|path
operator|.
name|getName
argument_list|()
operator|.
name|startsWith
argument_list|(
name|SNAPSHOT_MANIFEST_PREFIX
argument_list|)
return|;
block|}
block|}
argument_list|)
decl_stmt|;
if|if
condition|(
name|manifestFiles
operator|==
literal|null
operator|||
name|manifestFiles
operator|.
name|length
operator|==
literal|0
condition|)
return|return
literal|null
return|;
specifier|final
name|ExecutorCompletionService
argument_list|<
name|SnapshotRegionManifest
argument_list|>
name|completionService
init|=
operator|new
name|ExecutorCompletionService
argument_list|<
name|SnapshotRegionManifest
argument_list|>
argument_list|(
name|executor
argument_list|)
decl_stmt|;
for|for
control|(
specifier|final
name|FileStatus
name|st
range|:
name|manifestFiles
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
name|FSDataInputStream
name|stream
init|=
name|fs
operator|.
name|open
argument_list|(
name|st
operator|.
name|getPath
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
return|return
name|SnapshotRegionManifest
operator|.
name|parseFrom
argument_list|(
name|stream
argument_list|)
return|;
block|}
finally|finally
block|{
name|stream
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
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
argument_list|<
name|SnapshotRegionManifest
argument_list|>
argument_list|(
name|manifestFiles
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
name|manifestFiles
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
name|fs
operator|.
name|delete
argument_list|(
name|getRegionManifestPath
argument_list|(
name|snapshotDir
argument_list|,
name|manifest
argument_list|)
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|Path
name|getRegionManifestPath
parameter_list|(
specifier|final
name|Path
name|snapshotDir
parameter_list|,
specifier|final
name|SnapshotRegionManifest
name|manifest
parameter_list|)
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
return|return
operator|new
name|Path
argument_list|(
name|snapshotDir
argument_list|,
name|SNAPSHOT_MANIFEST_PREFIX
operator|+
name|regionName
argument_list|)
return|;
block|}
block|}
end_class

end_unit

