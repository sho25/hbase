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
name|FileNotFoundException
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
name|HashSet
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
name|Set
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
name|ExecutorService
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
name|io
operator|.
name|HFileLink
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
name|mob
operator|.
name|MobUtils
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
name|HFileArchiveUtil
import|;
end_import

begin_comment
comment|/**  * Utility methods for interacting with the snapshot referenced files.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|final
class|class
name|SnapshotReferenceUtil
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
name|SnapshotReferenceUtil
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
interface|interface
name|StoreFileVisitor
block|{
name|void
name|storeFile
parameter_list|(
specifier|final
name|HRegionInfo
name|regionInfo
parameter_list|,
specifier|final
name|String
name|familyName
parameter_list|,
specifier|final
name|SnapshotRegionManifest
operator|.
name|StoreFile
name|storeFile
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
specifier|public
interface|interface
name|SnapshotVisitor
extends|extends
name|StoreFileVisitor
block|{   }
specifier|private
name|SnapshotReferenceUtil
parameter_list|()
block|{
comment|// private constructor for utility class
block|}
comment|/**    * Iterate over the snapshot store files    *    * @param conf The current {@link Configuration} instance.    * @param fs {@link FileSystem}    * @param snapshotDir {@link Path} to the Snapshot directory    * @param visitor callback object to get the referenced files    * @throws IOException if an error occurred while scanning the directory    */
specifier|public
specifier|static
name|void
name|visitReferencedFiles
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
parameter_list|,
specifier|final
name|SnapshotVisitor
name|visitor
parameter_list|)
throws|throws
name|IOException
block|{
name|SnapshotDescription
name|desc
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
name|visitReferencedFiles
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|snapshotDir
argument_list|,
name|desc
argument_list|,
name|visitor
argument_list|)
expr_stmt|;
block|}
comment|/**    * Iterate over the snapshot store files, restored.edits and logs    *    * @param conf The current {@link Configuration} instance.    * @param fs {@link FileSystem}    * @param snapshotDir {@link Path} to the Snapshot directory    * @param desc the {@link SnapshotDescription} of the snapshot to verify    * @param visitor callback object to get the referenced files    * @throws IOException if an error occurred while scanning the directory    */
specifier|public
specifier|static
name|void
name|visitReferencedFiles
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
parameter_list|,
specifier|final
name|SnapshotDescription
name|desc
parameter_list|,
specifier|final
name|SnapshotVisitor
name|visitor
parameter_list|)
throws|throws
name|IOException
block|{
name|visitTableStoreFiles
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|snapshotDir
argument_list|,
name|desc
argument_list|,
name|visitor
argument_list|)
expr_stmt|;
block|}
comment|/**©    * Iterate over the snapshot store files    *    * @param conf The current {@link Configuration} instance.    * @param fs {@link FileSystem}    * @param snapshotDir {@link Path} to the Snapshot directory    * @param desc the {@link SnapshotDescription} of the snapshot to verify    * @param visitor callback object to get the store files    * @throws IOException if an error occurred while scanning the directory    */
specifier|static
name|void
name|visitTableStoreFiles
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
parameter_list|,
specifier|final
name|SnapshotDescription
name|desc
parameter_list|,
specifier|final
name|StoreFileVisitor
name|visitor
parameter_list|)
throws|throws
name|IOException
block|{
name|SnapshotManifest
name|manifest
init|=
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
name|desc
argument_list|)
decl_stmt|;
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
operator|||
name|regionManifests
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"No manifest files present: "
operator|+
name|snapshotDir
argument_list|)
expr_stmt|;
return|return;
block|}
for|for
control|(
name|SnapshotRegionManifest
name|regionManifest
range|:
name|regionManifests
control|)
block|{
name|visitRegionStoreFiles
argument_list|(
name|regionManifest
argument_list|,
name|visitor
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Iterate over the snapshot store files in the specified region    *    * @param manifest snapshot manifest to inspect    * @param visitor callback object to get the store files    * @throws IOException if an error occurred while scanning the directory    */
specifier|static
name|void
name|visitRegionStoreFiles
parameter_list|(
specifier|final
name|SnapshotRegionManifest
name|manifest
parameter_list|,
specifier|final
name|StoreFileVisitor
name|visitor
parameter_list|)
throws|throws
name|IOException
block|{
name|HRegionInfo
name|regionInfo
init|=
name|HRegionInfo
operator|.
name|convert
argument_list|(
name|manifest
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|SnapshotRegionManifest
operator|.
name|FamilyFiles
name|familyFiles
range|:
name|manifest
operator|.
name|getFamilyFilesList
argument_list|()
control|)
block|{
name|String
name|familyName
init|=
name|familyFiles
operator|.
name|getFamilyName
argument_list|()
operator|.
name|toStringUtf8
argument_list|()
decl_stmt|;
for|for
control|(
name|SnapshotRegionManifest
operator|.
name|StoreFile
name|storeFile
range|:
name|familyFiles
operator|.
name|getStoreFilesList
argument_list|()
control|)
block|{
name|visitor
operator|.
name|storeFile
argument_list|(
name|regionInfo
argument_list|,
name|familyName
argument_list|,
name|storeFile
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Verify the validity of the snapshot    *    * @param conf The current {@link Configuration} instance.    * @param fs {@link FileSystem}    * @param snapshotDir {@link Path} to the Snapshot directory of the snapshot to verify    * @param snapshotDesc the {@link SnapshotDescription} of the snapshot to verify    * @throws CorruptedSnapshotException if the snapshot is corrupted    * @throws IOException if an error occurred while scanning the directory    */
specifier|public
specifier|static
name|void
name|verifySnapshot
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
parameter_list|,
specifier|final
name|SnapshotDescription
name|snapshotDesc
parameter_list|)
throws|throws
name|IOException
block|{
name|SnapshotManifest
name|manifest
init|=
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
decl_stmt|;
name|verifySnapshot
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|manifest
argument_list|)
expr_stmt|;
block|}
comment|/**    * Verify the validity of the snapshot    *    * @param conf The current {@link Configuration} instance.    * @param fs {@link FileSystem}    * @param manifest snapshot manifest to inspect    * @throws CorruptedSnapshotException if the snapshot is corrupted    * @throws IOException if an error occurred while scanning the directory    */
specifier|public
specifier|static
name|void
name|verifySnapshot
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
name|SnapshotManifest
name|manifest
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|SnapshotDescription
name|snapshotDesc
init|=
name|manifest
operator|.
name|getSnapshotDescription
argument_list|()
decl_stmt|;
specifier|final
name|Path
name|snapshotDir
init|=
name|manifest
operator|.
name|getSnapshotDir
argument_list|()
decl_stmt|;
name|concurrentVisitReferencedFiles
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|manifest
argument_list|,
literal|"VerifySnapshot"
argument_list|,
operator|new
name|StoreFileVisitor
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|storeFile
parameter_list|(
specifier|final
name|HRegionInfo
name|regionInfo
parameter_list|,
specifier|final
name|String
name|family
parameter_list|,
specifier|final
name|SnapshotRegionManifest
operator|.
name|StoreFile
name|storeFile
parameter_list|)
throws|throws
name|IOException
block|{
name|verifyStoreFile
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|snapshotDir
argument_list|,
name|snapshotDesc
argument_list|,
name|regionInfo
argument_list|,
name|family
argument_list|,
name|storeFile
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|concurrentVisitReferencedFiles
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
name|SnapshotManifest
name|manifest
parameter_list|,
specifier|final
name|String
name|desc
parameter_list|,
specifier|final
name|StoreFileVisitor
name|visitor
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|Path
name|snapshotDir
init|=
name|manifest
operator|.
name|getSnapshotDir
argument_list|()
decl_stmt|;
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
operator|||
name|regionManifests
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"No manifest files present: "
operator|+
name|snapshotDir
argument_list|)
expr_stmt|;
return|return;
block|}
name|ExecutorService
name|exec
init|=
name|SnapshotManifest
operator|.
name|createExecutor
argument_list|(
name|conf
argument_list|,
name|desc
argument_list|)
decl_stmt|;
try|try
block|{
name|concurrentVisitReferencedFiles
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|manifest
argument_list|,
name|exec
argument_list|,
name|visitor
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|exec
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
name|void
name|concurrentVisitReferencedFiles
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
name|SnapshotManifest
name|manifest
parameter_list|,
specifier|final
name|ExecutorService
name|exec
parameter_list|,
specifier|final
name|StoreFileVisitor
name|visitor
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|SnapshotDescription
name|snapshotDesc
init|=
name|manifest
operator|.
name|getSnapshotDescription
argument_list|()
decl_stmt|;
specifier|final
name|Path
name|snapshotDir
init|=
name|manifest
operator|.
name|getSnapshotDir
argument_list|()
decl_stmt|;
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
operator|||
name|regionManifests
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"No manifest files present: "
operator|+
name|snapshotDir
argument_list|)
expr_stmt|;
return|return;
block|}
specifier|final
name|ExecutorCompletionService
argument_list|<
name|Void
argument_list|>
name|completionService
init|=
operator|new
name|ExecutorCompletionService
argument_list|<>
argument_list|(
name|exec
argument_list|)
decl_stmt|;
for|for
control|(
specifier|final
name|SnapshotRegionManifest
name|regionManifest
range|:
name|regionManifests
control|)
block|{
name|completionService
operator|.
name|submit
argument_list|(
operator|new
name|Callable
argument_list|<
name|Void
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Void
name|call
parameter_list|()
throws|throws
name|IOException
block|{
name|visitRegionStoreFiles
argument_list|(
name|regionManifest
argument_list|,
name|visitor
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
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
name|regionManifests
operator|.
name|size
argument_list|()
condition|;
operator|++
name|i
control|)
block|{
name|completionService
operator|.
name|take
argument_list|()
operator|.
name|get
argument_list|()
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
if|if
condition|(
name|e
operator|.
name|getCause
argument_list|()
operator|instanceof
name|CorruptedSnapshotException
condition|)
block|{
throw|throw
operator|new
name|CorruptedSnapshotException
argument_list|(
name|e
operator|.
name|getCause
argument_list|()
operator|.
name|getMessage
argument_list|()
argument_list|,
name|ProtobufUtil
operator|.
name|createSnapshotDesc
argument_list|(
name|snapshotDesc
argument_list|)
argument_list|)
throw|;
block|}
else|else
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
block|}
block|}
comment|/**    * Verify the validity of the snapshot store file    *    * @param conf The current {@link Configuration} instance.    * @param fs {@link FileSystem}    * @param snapshotDir {@link Path} to the Snapshot directory of the snapshot to verify    * @param snapshot the {@link SnapshotDescription} of the snapshot to verify    * @param regionInfo {@link HRegionInfo} of the region that contains the store file    * @param family family that contains the store file    * @param storeFile the store file to verify    * @throws CorruptedSnapshotException if the snapshot is corrupted    * @throws IOException if an error occurred while scanning the directory    */
specifier|private
specifier|static
name|void
name|verifyStoreFile
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
parameter_list|,
specifier|final
name|SnapshotDescription
name|snapshot
parameter_list|,
specifier|final
name|HRegionInfo
name|regionInfo
parameter_list|,
specifier|final
name|String
name|family
parameter_list|,
specifier|final
name|SnapshotRegionManifest
operator|.
name|StoreFile
name|storeFile
parameter_list|)
throws|throws
name|IOException
block|{
name|TableName
name|table
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|snapshot
operator|.
name|getTable
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|fileName
init|=
name|storeFile
operator|.
name|getName
argument_list|()
decl_stmt|;
name|Path
name|refPath
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|StoreFileInfo
operator|.
name|isReference
argument_list|(
name|fileName
argument_list|)
condition|)
block|{
comment|// If is a reference file check if the parent file is present in the snapshot
name|refPath
operator|=
operator|new
name|Path
argument_list|(
operator|new
name|Path
argument_list|(
name|regionInfo
operator|.
name|getEncodedName
argument_list|()
argument_list|,
name|family
argument_list|)
argument_list|,
name|fileName
argument_list|)
expr_stmt|;
name|refPath
operator|=
name|StoreFileInfo
operator|.
name|getReferredToFile
argument_list|(
name|refPath
argument_list|)
expr_stmt|;
name|String
name|refRegion
init|=
name|refPath
operator|.
name|getParent
argument_list|()
operator|.
name|getParent
argument_list|()
operator|.
name|getName
argument_list|()
decl_stmt|;
name|refPath
operator|=
name|HFileLink
operator|.
name|createPath
argument_list|(
name|table
argument_list|,
name|refRegion
argument_list|,
name|family
argument_list|,
name|refPath
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|HFileLink
operator|.
name|buildFromHFileLinkPattern
argument_list|(
name|conf
argument_list|,
name|refPath
argument_list|)
operator|.
name|exists
argument_list|(
name|fs
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|CorruptedSnapshotException
argument_list|(
literal|"Missing parent hfile for: "
operator|+
name|fileName
operator|+
literal|" path="
operator|+
name|refPath
argument_list|,
name|ProtobufUtil
operator|.
name|createSnapshotDesc
argument_list|(
name|snapshot
argument_list|)
argument_list|)
throw|;
block|}
if|if
condition|(
name|storeFile
operator|.
name|hasReference
argument_list|()
condition|)
block|{
comment|// We don't really need to look for the file on-disk
comment|// we already have the Reference information embedded here.
return|return;
block|}
block|}
name|Path
name|linkPath
decl_stmt|;
if|if
condition|(
name|refPath
operator|!=
literal|null
operator|&&
name|HFileLink
operator|.
name|isHFileLink
argument_list|(
name|refPath
argument_list|)
condition|)
block|{
name|linkPath
operator|=
operator|new
name|Path
argument_list|(
name|family
argument_list|,
name|refPath
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|HFileLink
operator|.
name|isHFileLink
argument_list|(
name|fileName
argument_list|)
condition|)
block|{
name|linkPath
operator|=
operator|new
name|Path
argument_list|(
name|family
argument_list|,
name|fileName
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|linkPath
operator|=
operator|new
name|Path
argument_list|(
name|family
argument_list|,
name|HFileLink
operator|.
name|createHFileLinkName
argument_list|(
name|table
argument_list|,
name|regionInfo
operator|.
name|getEncodedName
argument_list|()
argument_list|,
name|fileName
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// check if the linked file exists (in the archive, or in the table dir)
name|HFileLink
name|link
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|MobUtils
operator|.
name|isMobRegionInfo
argument_list|(
name|regionInfo
argument_list|)
condition|)
block|{
comment|// for mob region
name|link
operator|=
name|HFileLink
operator|.
name|buildFromHFileLinkPattern
argument_list|(
name|MobUtils
operator|.
name|getQualifiedMobRootDir
argument_list|(
name|conf
argument_list|)
argument_list|,
name|HFileArchiveUtil
operator|.
name|getArchivePath
argument_list|(
name|conf
argument_list|)
argument_list|,
name|linkPath
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// not mob region
name|link
operator|=
name|HFileLink
operator|.
name|buildFromHFileLinkPattern
argument_list|(
name|conf
argument_list|,
name|linkPath
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|FileStatus
name|fstat
init|=
name|link
operator|.
name|getFileStatus
argument_list|(
name|fs
argument_list|)
decl_stmt|;
if|if
condition|(
name|storeFile
operator|.
name|hasFileSize
argument_list|()
operator|&&
name|storeFile
operator|.
name|getFileSize
argument_list|()
operator|!=
name|fstat
operator|.
name|getLen
argument_list|()
condition|)
block|{
name|String
name|msg
init|=
literal|"hfile: "
operator|+
name|fileName
operator|+
literal|" size does not match with the expected one. "
operator|+
literal|" found="
operator|+
name|fstat
operator|.
name|getLen
argument_list|()
operator|+
literal|" expected="
operator|+
name|storeFile
operator|.
name|getFileSize
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|msg
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|CorruptedSnapshotException
argument_list|(
name|msg
argument_list|,
name|ProtobufUtil
operator|.
name|createSnapshotDesc
argument_list|(
name|snapshot
argument_list|)
argument_list|)
throw|;
block|}
block|}
catch|catch
parameter_list|(
name|FileNotFoundException
name|e
parameter_list|)
block|{
name|String
name|msg
init|=
literal|"Can't find hfile: "
operator|+
name|fileName
operator|+
literal|" in the real ("
operator|+
name|link
operator|.
name|getOriginPath
argument_list|()
operator|+
literal|") or archive ("
operator|+
name|link
operator|.
name|getArchivePath
argument_list|()
operator|+
literal|") directory for the primary table."
decl_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|msg
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|CorruptedSnapshotException
argument_list|(
name|msg
argument_list|,
name|ProtobufUtil
operator|.
name|createSnapshotDesc
argument_list|(
name|snapshot
argument_list|)
argument_list|)
throw|;
block|}
block|}
comment|/**    * Returns the store file names in the snapshot.    *    * @param conf The current {@link Configuration} instance.    * @param fs {@link FileSystem}    * @param snapshotDir {@link Path} to the Snapshot directory    * @throws IOException if an error occurred while scanning the directory    * @return the names of hfiles in the specified snaphot    */
specifier|public
specifier|static
name|Set
argument_list|<
name|String
argument_list|>
name|getHFileNames
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
throws|throws
name|IOException
block|{
name|SnapshotDescription
name|desc
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
name|getHFileNames
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|snapshotDir
argument_list|,
name|desc
argument_list|)
return|;
block|}
comment|/**    * Returns the store file names in the snapshot.    *    * @param conf The current {@link Configuration} instance.    * @param fs {@link FileSystem}    * @param snapshotDir {@link Path} to the Snapshot directory    * @param snapshotDesc the {@link SnapshotDescription} of the snapshot to inspect    * @throws IOException if an error occurred while scanning the directory    * @return the names of hfiles in the specified snaphot    */
specifier|private
specifier|static
name|Set
argument_list|<
name|String
argument_list|>
name|getHFileNames
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
parameter_list|,
specifier|final
name|SnapshotDescription
name|snapshotDesc
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|names
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
name|visitTableStoreFiles
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|snapshotDir
argument_list|,
name|snapshotDesc
argument_list|,
operator|new
name|StoreFileVisitor
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|storeFile
parameter_list|(
specifier|final
name|HRegionInfo
name|regionInfo
parameter_list|,
specifier|final
name|String
name|family
parameter_list|,
specifier|final
name|SnapshotRegionManifest
operator|.
name|StoreFile
name|storeFile
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|hfile
init|=
name|storeFile
operator|.
name|getName
argument_list|()
decl_stmt|;
if|if
condition|(
name|HFileLink
operator|.
name|isHFileLink
argument_list|(
name|hfile
argument_list|)
condition|)
block|{
name|names
operator|.
name|add
argument_list|(
name|HFileLink
operator|.
name|getReferencedHFileName
argument_list|(
name|hfile
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|names
operator|.
name|add
argument_list|(
name|hfile
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
return|return
name|names
return|;
block|}
block|}
end_class

end_unit

