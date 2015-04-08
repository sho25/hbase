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
name|util
operator|.
name|regex
operator|.
name|Matcher
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
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
name|io
operator|.
name|FSDataInputStreamWrapper
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
name|io
operator|.
name|HalfStoreFileReader
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
name|Reference
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
name|util
operator|.
name|FSUtils
import|;
end_import

begin_comment
comment|/**  * Describe a StoreFile (hfile, reference, link)  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|StoreFileInfo
block|{
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
name|StoreFileInfo
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * A non-capture group, for hfiles, so that this can be embedded.    * HFiles are uuid ([0-9a-z]+). Bulk loaded hfiles has (_SeqId_[0-9]+_) has suffix.    * The mob del file has (_del) as suffix.    */
specifier|public
specifier|static
specifier|final
name|String
name|HFILE_NAME_REGEX
init|=
literal|"[0-9a-f]+(?:(?:_SeqId_[0-9]+_)|(?:_del))?"
decl_stmt|;
comment|/** Regex that will work for hfiles */
specifier|private
specifier|static
specifier|final
name|Pattern
name|HFILE_NAME_PATTERN
init|=
name|Pattern
operator|.
name|compile
argument_list|(
literal|"^("
operator|+
name|HFILE_NAME_REGEX
operator|+
literal|")"
argument_list|)
decl_stmt|;
comment|/**    * A non-capture group, for hfiles, so that this can be embedded.    * A del file has (_del) as suffix.    */
specifier|public
specifier|static
specifier|final
name|String
name|DELFILE_NAME_REGEX
init|=
literal|"[0-9a-f]+(?:_del)"
decl_stmt|;
comment|/** Regex that will work for del files */
specifier|private
specifier|static
specifier|final
name|Pattern
name|DELFILE_NAME_PATTERN
init|=
name|Pattern
operator|.
name|compile
argument_list|(
literal|"^("
operator|+
name|DELFILE_NAME_REGEX
operator|+
literal|")"
argument_list|)
decl_stmt|;
comment|/**    * Regex that will work for straight reference names (<hfile>.<parentEncRegion>)    * and hfilelink reference names (<table>=<region>-<hfile>.<parentEncRegion>)    * If reference, then the regex has more than just one group.    * Group 1, hfile/hfilelink pattern, is this file's id.    * Group 2 '(.+)' is the reference's parent region name.    */
specifier|private
specifier|static
specifier|final
name|Pattern
name|REF_NAME_PATTERN
init|=
name|Pattern
operator|.
name|compile
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"^(%s|%s)\\.(.+)$"
argument_list|,
name|HFILE_NAME_REGEX
argument_list|,
name|HFileLink
operator|.
name|LINK_NAME_REGEX
argument_list|)
argument_list|)
decl_stmt|;
comment|// Configuration
specifier|private
name|Configuration
name|conf
decl_stmt|;
comment|// FileSystem handle
specifier|private
specifier|final
name|FileSystem
name|fs
decl_stmt|;
comment|// HDFS blocks distribution information
specifier|private
name|HDFSBlocksDistribution
name|hdfsBlocksDistribution
init|=
literal|null
decl_stmt|;
comment|// If this storefile references another, this is the reference instance.
specifier|private
specifier|final
name|Reference
name|reference
decl_stmt|;
comment|// If this storefile is a link to another, this is the link instance.
specifier|private
specifier|final
name|HFileLink
name|link
decl_stmt|;
specifier|private
specifier|final
name|Path
name|initialPath
decl_stmt|;
specifier|private
name|RegionCoprocessorHost
name|coprocessorHost
decl_stmt|;
comment|/**    * Create a Store File Info    * @param conf the {@link Configuration} to use    * @param fs The current file system to use.    * @param initialPath The {@link Path} of the file    */
specifier|public
name|StoreFileInfo
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
name|initialPath
parameter_list|)
throws|throws
name|IOException
block|{
assert|assert
name|fs
operator|!=
literal|null
assert|;
assert|assert
name|initialPath
operator|!=
literal|null
assert|;
assert|assert
name|conf
operator|!=
literal|null
assert|;
name|this
operator|.
name|fs
operator|=
name|fs
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|initialPath
operator|=
name|initialPath
expr_stmt|;
name|Path
name|p
init|=
name|initialPath
decl_stmt|;
if|if
condition|(
name|HFileLink
operator|.
name|isHFileLink
argument_list|(
name|p
argument_list|)
condition|)
block|{
comment|// HFileLink
name|this
operator|.
name|reference
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|link
operator|=
name|HFileLink
operator|.
name|buildFromHFileLinkPattern
argument_list|(
name|conf
argument_list|,
name|p
argument_list|)
expr_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
name|LOG
operator|.
name|trace
argument_list|(
name|p
operator|+
literal|" is a link"
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|isReference
argument_list|(
name|p
argument_list|)
condition|)
block|{
name|this
operator|.
name|reference
operator|=
name|Reference
operator|.
name|read
argument_list|(
name|fs
argument_list|,
name|p
argument_list|)
expr_stmt|;
name|Path
name|referencePath
init|=
name|getReferredToFile
argument_list|(
name|p
argument_list|)
decl_stmt|;
if|if
condition|(
name|HFileLink
operator|.
name|isHFileLink
argument_list|(
name|referencePath
argument_list|)
condition|)
block|{
comment|// HFileLink Reference
name|this
operator|.
name|link
operator|=
name|HFileLink
operator|.
name|buildFromHFileLinkPattern
argument_list|(
name|conf
argument_list|,
name|referencePath
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// Reference
name|this
operator|.
name|link
operator|=
literal|null
expr_stmt|;
block|}
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
name|LOG
operator|.
name|trace
argument_list|(
name|p
operator|+
literal|" is a "
operator|+
name|reference
operator|.
name|getFileRegion
argument_list|()
operator|+
literal|" reference to "
operator|+
name|referencePath
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|isHFile
argument_list|(
name|p
argument_list|)
condition|)
block|{
comment|// HFile
name|this
operator|.
name|reference
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|link
operator|=
literal|null
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"path="
operator|+
name|p
operator|+
literal|" doesn't look like a valid StoreFile"
argument_list|)
throw|;
block|}
block|}
comment|/**    * Create a Store File Info    * @param conf the {@link Configuration} to use    * @param fs The current file system to use.    * @param fileStatus The {@link FileStatus} of the file    */
specifier|public
name|StoreFileInfo
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
name|FileStatus
name|fileStatus
parameter_list|)
throws|throws
name|IOException
block|{
name|this
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|fileStatus
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create a Store File Info from an HFileLink    * @param conf the {@link Configuration} to use    * @param fs The current file system to use.    * @param fileStatus The {@link FileStatus} of the file    */
specifier|public
name|StoreFileInfo
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
name|FileStatus
name|fileStatus
parameter_list|,
specifier|final
name|HFileLink
name|link
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|fs
operator|=
name|fs
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
comment|// initialPath can be null only if we get a link.
name|this
operator|.
name|initialPath
operator|=
operator|(
name|fileStatus
operator|==
literal|null
operator|)
condition|?
literal|null
else|:
name|fileStatus
operator|.
name|getPath
argument_list|()
expr_stmt|;
comment|// HFileLink
name|this
operator|.
name|reference
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|link
operator|=
name|link
expr_stmt|;
block|}
comment|/**    * Create a Store File Info from an HFileLink    * @param conf    * @param fs    * @param fileStatus    * @param reference    * @throws IOException    */
specifier|public
name|StoreFileInfo
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
name|FileStatus
name|fileStatus
parameter_list|,
specifier|final
name|Reference
name|reference
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|fs
operator|=
name|fs
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|initialPath
operator|=
name|fileStatus
operator|.
name|getPath
argument_list|()
expr_stmt|;
name|this
operator|.
name|reference
operator|=
name|reference
expr_stmt|;
name|this
operator|.
name|link
operator|=
literal|null
expr_stmt|;
block|}
comment|/**    * Sets the region coprocessor env.    * @param coprocessorHost    */
specifier|public
name|void
name|setRegionCoprocessorHost
parameter_list|(
name|RegionCoprocessorHost
name|coprocessorHost
parameter_list|)
block|{
name|this
operator|.
name|coprocessorHost
operator|=
name|coprocessorHost
expr_stmt|;
block|}
comment|/*    * @return the Reference object associated to this StoreFileInfo.    *         null if the StoreFile is not a reference.    */
specifier|public
name|Reference
name|getReference
parameter_list|()
block|{
return|return
name|this
operator|.
name|reference
return|;
block|}
comment|/** @return True if the store file is a Reference */
specifier|public
name|boolean
name|isReference
parameter_list|()
block|{
return|return
name|this
operator|.
name|reference
operator|!=
literal|null
return|;
block|}
comment|/** @return True if the store file is a top Reference */
specifier|public
name|boolean
name|isTopReference
parameter_list|()
block|{
return|return
name|this
operator|.
name|reference
operator|!=
literal|null
operator|&&
name|Reference
operator|.
name|isTopFileRegion
argument_list|(
name|this
operator|.
name|reference
operator|.
name|getFileRegion
argument_list|()
argument_list|)
return|;
block|}
comment|/** @return True if the store file is a link */
specifier|public
name|boolean
name|isLink
parameter_list|()
block|{
return|return
name|this
operator|.
name|link
operator|!=
literal|null
operator|&&
name|this
operator|.
name|reference
operator|==
literal|null
return|;
block|}
comment|/** @return the HDFS block distribution */
specifier|public
name|HDFSBlocksDistribution
name|getHDFSBlockDistribution
parameter_list|()
block|{
return|return
name|this
operator|.
name|hdfsBlocksDistribution
return|;
block|}
comment|/**    * Open a Reader for the StoreFile    * @param fs The current file system to use.    * @param cacheConf The cache configuration and block cache reference.    * @return The StoreFile.Reader for the file    */
specifier|public
name|StoreFile
operator|.
name|Reader
name|open
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|CacheConfig
name|cacheConf
parameter_list|)
throws|throws
name|IOException
block|{
name|FSDataInputStreamWrapper
name|in
decl_stmt|;
name|FileStatus
name|status
decl_stmt|;
if|if
condition|(
name|this
operator|.
name|link
operator|!=
literal|null
condition|)
block|{
comment|// HFileLink
name|in
operator|=
operator|new
name|FSDataInputStreamWrapper
argument_list|(
name|fs
argument_list|,
name|this
operator|.
name|link
argument_list|)
expr_stmt|;
name|status
operator|=
name|this
operator|.
name|link
operator|.
name|getFileStatus
argument_list|(
name|fs
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|this
operator|.
name|reference
operator|!=
literal|null
condition|)
block|{
comment|// HFile Reference
name|Path
name|referencePath
init|=
name|getReferredToFile
argument_list|(
name|this
operator|.
name|getPath
argument_list|()
argument_list|)
decl_stmt|;
name|in
operator|=
operator|new
name|FSDataInputStreamWrapper
argument_list|(
name|fs
argument_list|,
name|referencePath
argument_list|)
expr_stmt|;
name|status
operator|=
name|fs
operator|.
name|getFileStatus
argument_list|(
name|referencePath
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|in
operator|=
operator|new
name|FSDataInputStreamWrapper
argument_list|(
name|fs
argument_list|,
name|this
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
name|status
operator|=
name|fs
operator|.
name|getFileStatus
argument_list|(
name|initialPath
argument_list|)
expr_stmt|;
block|}
name|long
name|length
init|=
name|status
operator|.
name|getLen
argument_list|()
decl_stmt|;
name|hdfsBlocksDistribution
operator|=
name|computeHDFSBlocksDistribution
argument_list|(
name|fs
argument_list|)
expr_stmt|;
name|StoreFile
operator|.
name|Reader
name|reader
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|this
operator|.
name|coprocessorHost
operator|!=
literal|null
condition|)
block|{
name|reader
operator|=
name|this
operator|.
name|coprocessorHost
operator|.
name|preStoreFileReaderOpen
argument_list|(
name|fs
argument_list|,
name|this
operator|.
name|getPath
argument_list|()
argument_list|,
name|in
argument_list|,
name|length
argument_list|,
name|cacheConf
argument_list|,
name|reference
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|reader
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|this
operator|.
name|reference
operator|!=
literal|null
condition|)
block|{
name|reader
operator|=
operator|new
name|HalfStoreFileReader
argument_list|(
name|fs
argument_list|,
name|this
operator|.
name|getPath
argument_list|()
argument_list|,
name|in
argument_list|,
name|length
argument_list|,
name|cacheConf
argument_list|,
name|reference
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|reader
operator|=
operator|new
name|StoreFile
operator|.
name|Reader
argument_list|(
name|fs
argument_list|,
name|status
operator|.
name|getPath
argument_list|()
argument_list|,
name|in
argument_list|,
name|length
argument_list|,
name|cacheConf
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|this
operator|.
name|coprocessorHost
operator|!=
literal|null
condition|)
block|{
name|reader
operator|=
name|this
operator|.
name|coprocessorHost
operator|.
name|postStoreFileReaderOpen
argument_list|(
name|fs
argument_list|,
name|this
operator|.
name|getPath
argument_list|()
argument_list|,
name|in
argument_list|,
name|length
argument_list|,
name|cacheConf
argument_list|,
name|reference
argument_list|,
name|reader
argument_list|)
expr_stmt|;
block|}
return|return
name|reader
return|;
block|}
comment|/**    * Compute the HDFS Block Distribution for this StoreFile    */
specifier|public
name|HDFSBlocksDistribution
name|computeHDFSBlocksDistribution
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|)
throws|throws
name|IOException
block|{
comment|// guard against the case where we get the FileStatus from link, but by the time we
comment|// call compute the file is moved again
if|if
condition|(
name|this
operator|.
name|link
operator|!=
literal|null
condition|)
block|{
name|FileNotFoundException
name|exToThrow
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
name|this
operator|.
name|link
operator|.
name|getLocations
argument_list|()
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
try|try
block|{
return|return
name|computeHDFSBlocksDistributionInternal
argument_list|(
name|fs
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|FileNotFoundException
name|ex
parameter_list|)
block|{
comment|// try the other location
name|exToThrow
operator|=
name|ex
expr_stmt|;
block|}
block|}
throw|throw
name|exToThrow
throw|;
block|}
else|else
block|{
return|return
name|computeHDFSBlocksDistributionInternal
argument_list|(
name|fs
argument_list|)
return|;
block|}
block|}
specifier|private
name|HDFSBlocksDistribution
name|computeHDFSBlocksDistributionInternal
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|)
throws|throws
name|IOException
block|{
name|FileStatus
name|status
init|=
name|getReferencedFileStatus
argument_list|(
name|fs
argument_list|)
decl_stmt|;
if|if
condition|(
name|this
operator|.
name|reference
operator|!=
literal|null
condition|)
block|{
return|return
name|computeRefFileHDFSBlockDistribution
argument_list|(
name|fs
argument_list|,
name|reference
argument_list|,
name|status
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|FSUtils
operator|.
name|computeHDFSBlocksDistribution
argument_list|(
name|fs
argument_list|,
name|status
argument_list|,
literal|0
argument_list|,
name|status
operator|.
name|getLen
argument_list|()
argument_list|)
return|;
block|}
block|}
comment|/**    * Get the {@link FileStatus} of the file referenced by this StoreFileInfo    * @param fs The current file system to use.    * @return The {@link FileStatus} of the file referenced by this StoreFileInfo    */
specifier|public
name|FileStatus
name|getReferencedFileStatus
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|)
throws|throws
name|IOException
block|{
name|FileStatus
name|status
decl_stmt|;
if|if
condition|(
name|this
operator|.
name|reference
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|this
operator|.
name|link
operator|!=
literal|null
condition|)
block|{
name|FileNotFoundException
name|exToThrow
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
name|this
operator|.
name|link
operator|.
name|getLocations
argument_list|()
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
comment|// HFileLink Reference
try|try
block|{
return|return
name|link
operator|.
name|getFileStatus
argument_list|(
name|fs
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|FileNotFoundException
name|ex
parameter_list|)
block|{
comment|// try the other location
name|exToThrow
operator|=
name|ex
expr_stmt|;
block|}
block|}
throw|throw
name|exToThrow
throw|;
block|}
else|else
block|{
comment|// HFile Reference
name|Path
name|referencePath
init|=
name|getReferredToFile
argument_list|(
name|this
operator|.
name|getPath
argument_list|()
argument_list|)
decl_stmt|;
name|status
operator|=
name|fs
operator|.
name|getFileStatus
argument_list|(
name|referencePath
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
if|if
condition|(
name|this
operator|.
name|link
operator|!=
literal|null
condition|)
block|{
name|FileNotFoundException
name|exToThrow
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
name|this
operator|.
name|link
operator|.
name|getLocations
argument_list|()
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
comment|// HFileLink
try|try
block|{
return|return
name|link
operator|.
name|getFileStatus
argument_list|(
name|fs
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|FileNotFoundException
name|ex
parameter_list|)
block|{
comment|// try the other location
name|exToThrow
operator|=
name|ex
expr_stmt|;
block|}
block|}
throw|throw
name|exToThrow
throw|;
block|}
else|else
block|{
name|status
operator|=
name|fs
operator|.
name|getFileStatus
argument_list|(
name|initialPath
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|status
return|;
block|}
comment|/** @return The {@link Path} of the file */
specifier|public
name|Path
name|getPath
parameter_list|()
block|{
return|return
name|initialPath
return|;
block|}
comment|/** @return The {@link FileStatus} of the file */
specifier|public
name|FileStatus
name|getFileStatus
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|getReferencedFileStatus
argument_list|(
name|fs
argument_list|)
return|;
block|}
comment|/** @return Get the modification time of the file. */
specifier|public
name|long
name|getModificationTime
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|getFileStatus
argument_list|()
operator|.
name|getModificationTime
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|this
operator|.
name|getPath
argument_list|()
operator|+
operator|(
name|isReference
argument_list|()
condition|?
literal|"-"
operator|+
name|getReferredToFile
argument_list|(
name|this
operator|.
name|getPath
argument_list|()
argument_list|)
operator|+
literal|"-"
operator|+
name|reference
else|:
literal|""
operator|)
return|;
block|}
comment|/**    * @param path Path to check.    * @return True if the path has format of a HFile.    */
specifier|public
specifier|static
name|boolean
name|isHFile
parameter_list|(
specifier|final
name|Path
name|path
parameter_list|)
block|{
return|return
name|isHFile
argument_list|(
name|path
operator|.
name|getName
argument_list|()
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|boolean
name|isHFile
parameter_list|(
specifier|final
name|String
name|fileName
parameter_list|)
block|{
name|Matcher
name|m
init|=
name|HFILE_NAME_PATTERN
operator|.
name|matcher
argument_list|(
name|fileName
argument_list|)
decl_stmt|;
return|return
name|m
operator|.
name|matches
argument_list|()
operator|&&
name|m
operator|.
name|groupCount
argument_list|()
operator|>
literal|0
return|;
block|}
comment|/**    * @param path Path to check.    * @return True if the path has format of a del file.    */
specifier|public
specifier|static
name|boolean
name|isDelFile
parameter_list|(
specifier|final
name|Path
name|path
parameter_list|)
block|{
return|return
name|isDelFile
argument_list|(
name|path
operator|.
name|getName
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * @param fileName Sting version of path to validate.    * @return True if the file name has format of a del file.    */
specifier|public
specifier|static
name|boolean
name|isDelFile
parameter_list|(
specifier|final
name|String
name|fileName
parameter_list|)
block|{
name|Matcher
name|m
init|=
name|DELFILE_NAME_PATTERN
operator|.
name|matcher
argument_list|(
name|fileName
argument_list|)
decl_stmt|;
return|return
name|m
operator|.
name|matches
argument_list|()
operator|&&
name|m
operator|.
name|groupCount
argument_list|()
operator|>
literal|0
return|;
block|}
comment|/**    * @param path Path to check.    * @return True if the path has format of a HStoreFile reference.    */
specifier|public
specifier|static
name|boolean
name|isReference
parameter_list|(
specifier|final
name|Path
name|path
parameter_list|)
block|{
return|return
name|isReference
argument_list|(
name|path
operator|.
name|getName
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * @param name file name to check.    * @return True if the path has format of a HStoreFile reference.    */
specifier|public
specifier|static
name|boolean
name|isReference
parameter_list|(
specifier|final
name|String
name|name
parameter_list|)
block|{
name|Matcher
name|m
init|=
name|REF_NAME_PATTERN
operator|.
name|matcher
argument_list|(
name|name
argument_list|)
decl_stmt|;
return|return
name|m
operator|.
name|matches
argument_list|()
operator|&&
name|m
operator|.
name|groupCount
argument_list|()
operator|>
literal|1
return|;
block|}
comment|/*    * Return path to the file referred to by a Reference.  Presumes a directory    * hierarchy of<code>${hbase.rootdir}/data/${namespace}/tablename/regionname/familyname</code>.    * @param p Path to a Reference file.    * @return Calculated path to parent region file.    * @throws IllegalArgumentException when path regex fails to match.    */
specifier|public
specifier|static
name|Path
name|getReferredToFile
parameter_list|(
specifier|final
name|Path
name|p
parameter_list|)
block|{
name|Matcher
name|m
init|=
name|REF_NAME_PATTERN
operator|.
name|matcher
argument_list|(
name|p
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|m
operator|==
literal|null
operator|||
operator|!
name|m
operator|.
name|matches
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed match of store file name "
operator|+
name|p
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Failed match of store file name "
operator|+
name|p
operator|.
name|toString
argument_list|()
argument_list|)
throw|;
block|}
comment|// Other region name is suffix on the passed Reference file name
name|String
name|otherRegion
init|=
name|m
operator|.
name|group
argument_list|(
literal|2
argument_list|)
decl_stmt|;
comment|// Tabledir is up two directories from where Reference was written.
name|Path
name|tableDir
init|=
name|p
operator|.
name|getParent
argument_list|()
operator|.
name|getParent
argument_list|()
operator|.
name|getParent
argument_list|()
decl_stmt|;
name|String
name|nameStrippedOfSuffix
init|=
name|m
operator|.
name|group
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"reference '"
operator|+
name|p
operator|+
literal|"' to region="
operator|+
name|otherRegion
operator|+
literal|" hfile="
operator|+
name|nameStrippedOfSuffix
argument_list|)
expr_stmt|;
comment|// Build up new path with the referenced region in place of our current
comment|// region in the reference path.  Also strip regionname suffix from name.
return|return
operator|new
name|Path
argument_list|(
operator|new
name|Path
argument_list|(
operator|new
name|Path
argument_list|(
name|tableDir
argument_list|,
name|otherRegion
argument_list|)
argument_list|,
name|p
operator|.
name|getParent
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|,
name|nameStrippedOfSuffix
argument_list|)
return|;
block|}
comment|/**    * Validate the store file name.    * @param fileName name of the file to validate    * @return<tt>true</tt> if the file could be a valid store file,<tt>false</tt> otherwise    */
specifier|public
specifier|static
name|boolean
name|validateStoreFileName
parameter_list|(
specifier|final
name|String
name|fileName
parameter_list|)
block|{
if|if
condition|(
name|HFileLink
operator|.
name|isHFileLink
argument_list|(
name|fileName
argument_list|)
operator|||
name|isReference
argument_list|(
name|fileName
argument_list|)
condition|)
return|return
operator|(
literal|true
operator|)
return|;
return|return
operator|!
name|fileName
operator|.
name|contains
argument_list|(
literal|"-"
argument_list|)
return|;
block|}
comment|/**    * Return if the specified file is a valid store file or not.    * @param fileStatus The {@link FileStatus} of the file    * @return<tt>true</tt> if the file is valid    */
specifier|public
specifier|static
name|boolean
name|isValid
parameter_list|(
specifier|final
name|FileStatus
name|fileStatus
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|Path
name|p
init|=
name|fileStatus
operator|.
name|getPath
argument_list|()
decl_stmt|;
if|if
condition|(
name|fileStatus
operator|.
name|isDirectory
argument_list|()
condition|)
return|return
literal|false
return|;
comment|// Check for empty hfile. Should never be the case but can happen
comment|// after data loss in hdfs for whatever reason (upgrade, etc.): HBASE-646
comment|// NOTE: that the HFileLink is just a name, so it's an empty file.
if|if
condition|(
operator|!
name|HFileLink
operator|.
name|isHFileLink
argument_list|(
name|p
argument_list|)
operator|&&
name|fileStatus
operator|.
name|getLen
argument_list|()
operator|<=
literal|0
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Skipping "
operator|+
name|p
operator|+
literal|" because it is empty. HBASE-646 DATA LOSS?"
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
return|return
name|validateStoreFileName
argument_list|(
name|p
operator|.
name|getName
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * helper function to compute HDFS blocks distribution of a given reference    * file.For reference file, we don't compute the exact value. We use some    * estimate instead given it might be good enough. we assume bottom part    * takes the first half of reference file, top part takes the second half    * of the reference file. This is just estimate, given    * midkey ofregion != midkey of HFile, also the number and size of keys vary.    * If this estimate isn't good enough, we can improve it later.    * @param fs  The FileSystem    * @param reference  The reference    * @param status  The reference FileStatus    * @return HDFS blocks distribution    */
specifier|private
specifier|static
name|HDFSBlocksDistribution
name|computeRefFileHDFSBlockDistribution
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|Reference
name|reference
parameter_list|,
specifier|final
name|FileStatus
name|status
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|status
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|long
name|start
init|=
literal|0
decl_stmt|;
name|long
name|length
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|Reference
operator|.
name|isTopFileRegion
argument_list|(
name|reference
operator|.
name|getFileRegion
argument_list|()
argument_list|)
condition|)
block|{
name|start
operator|=
name|status
operator|.
name|getLen
argument_list|()
operator|/
literal|2
expr_stmt|;
name|length
operator|=
name|status
operator|.
name|getLen
argument_list|()
operator|-
name|status
operator|.
name|getLen
argument_list|()
operator|/
literal|2
expr_stmt|;
block|}
else|else
block|{
name|start
operator|=
literal|0
expr_stmt|;
name|length
operator|=
name|status
operator|.
name|getLen
argument_list|()
operator|/
literal|2
expr_stmt|;
block|}
return|return
name|FSUtils
operator|.
name|computeHDFSBlocksDistribution
argument_list|(
name|fs
argument_list|,
name|status
argument_list|,
name|start
argument_list|,
name|length
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|that
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|that
condition|)
return|return
literal|true
return|;
if|if
condition|(
name|that
operator|==
literal|null
condition|)
return|return
literal|false
return|;
if|if
condition|(
operator|!
operator|(
name|that
operator|instanceof
name|StoreFileInfo
operator|)
condition|)
return|return
literal|false
return|;
name|StoreFileInfo
name|o
init|=
operator|(
name|StoreFileInfo
operator|)
name|that
decl_stmt|;
if|if
condition|(
name|initialPath
operator|!=
literal|null
operator|&&
name|o
operator|.
name|initialPath
operator|==
literal|null
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|initialPath
operator|==
literal|null
operator|&&
name|o
operator|.
name|initialPath
operator|!=
literal|null
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|initialPath
operator|!=
name|o
operator|.
name|initialPath
operator|&&
name|initialPath
operator|!=
literal|null
operator|&&
operator|!
name|initialPath
operator|.
name|equals
argument_list|(
name|o
operator|.
name|initialPath
argument_list|)
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|reference
operator|!=
literal|null
operator|&&
name|o
operator|.
name|reference
operator|==
literal|null
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|reference
operator|==
literal|null
operator|&&
name|o
operator|.
name|reference
operator|!=
literal|null
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|reference
operator|!=
name|o
operator|.
name|reference
operator|&&
name|reference
operator|!=
literal|null
operator|&&
operator|!
name|reference
operator|.
name|equals
argument_list|(
name|o
operator|.
name|reference
argument_list|)
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|link
operator|!=
literal|null
operator|&&
name|o
operator|.
name|link
operator|==
literal|null
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|link
operator|==
literal|null
operator|&&
name|o
operator|.
name|link
operator|!=
literal|null
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|link
operator|!=
name|o
operator|.
name|link
operator|&&
name|link
operator|!=
literal|null
operator|&&
operator|!
name|link
operator|.
name|equals
argument_list|(
name|o
operator|.
name|link
argument_list|)
condition|)
return|return
literal|false
return|;
return|return
literal|true
return|;
block|}
empty_stmt|;
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
name|int
name|hash
init|=
literal|17
decl_stmt|;
name|hash
operator|=
name|hash
operator|*
literal|31
operator|+
operator|(
operator|(
name|reference
operator|==
literal|null
operator|)
condition|?
literal|0
else|:
name|reference
operator|.
name|hashCode
argument_list|()
operator|)
expr_stmt|;
name|hash
operator|=
name|hash
operator|*
literal|31
operator|+
operator|(
operator|(
name|initialPath
operator|==
literal|null
operator|)
condition|?
literal|0
else|:
name|initialPath
operator|.
name|hashCode
argument_list|()
operator|)
expr_stmt|;
name|hash
operator|=
name|hash
operator|*
literal|31
operator|+
operator|(
operator|(
name|link
operator|==
literal|null
operator|)
condition|?
literal|0
else|:
name|link
operator|.
name|hashCode
argument_list|()
operator|)
expr_stmt|;
return|return
name|hash
return|;
block|}
block|}
end_class

end_unit

