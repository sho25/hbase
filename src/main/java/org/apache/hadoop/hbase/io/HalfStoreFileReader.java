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
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
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
name|io
operator|.
name|encoding
operator|.
name|DataBlockEncoding
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
name|HFileScanner
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
name|StoreFile
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

begin_comment
comment|/**  * A facade for a {@link org.apache.hadoop.hbase.io.hfile.HFile.Reader} that serves up  * either the top or bottom half of a HFile where 'bottom' is the first half  * of the file containing the keys that sort lowest and 'top' is the second half  * of the file with keys that sort greater than those of the bottom half.  * The top includes the split files midkey, of the key that follows if it does  * not exist in the file.  *  *<p>This type works in tandem with the {@link Reference} type.  This class  * is used reading while Reference is used writing.  *  *<p>This file is not splitable.  Calls to {@link #midkey()} return null.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|HalfStoreFileReader
extends|extends
name|StoreFile
operator|.
name|Reader
block|{
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|HalfStoreFileReader
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|final
name|boolean
name|top
decl_stmt|;
comment|// This is the key we split around.  Its the first possible entry on a row:
comment|// i.e. empty column and a timestamp of LATEST_TIMESTAMP.
specifier|protected
specifier|final
name|byte
index|[]
name|splitkey
decl_stmt|;
comment|/**    * @param fs    * @param p    * @param cacheConf    * @param r    * @throws IOException    */
specifier|public
name|HalfStoreFileReader
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|Path
name|p
parameter_list|,
specifier|final
name|CacheConfig
name|cacheConf
parameter_list|,
specifier|final
name|Reference
name|r
parameter_list|,
name|DataBlockEncoding
name|preferredEncodingInCache
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|fs
argument_list|,
name|p
argument_list|,
name|cacheConf
argument_list|,
name|preferredEncodingInCache
argument_list|)
expr_stmt|;
comment|// This is not actual midkey for this half-file; its just border
comment|// around which we split top and bottom.  Have to look in files to find
comment|// actual last and first keys for bottom and top halves.  Half-files don't
comment|// have an actual midkey themselves. No midkey is how we indicate file is
comment|// not splittable.
name|this
operator|.
name|splitkey
operator|=
name|r
operator|.
name|getSplitKey
argument_list|()
expr_stmt|;
comment|// Is it top or bottom half?
name|this
operator|.
name|top
operator|=
name|Reference
operator|.
name|isTopFileRegion
argument_list|(
name|r
operator|.
name|getFileRegion
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|boolean
name|isTop
parameter_list|()
block|{
return|return
name|this
operator|.
name|top
return|;
block|}
annotation|@
name|Override
specifier|public
name|HFileScanner
name|getScanner
parameter_list|(
specifier|final
name|boolean
name|cacheBlocks
parameter_list|,
specifier|final
name|boolean
name|pread
parameter_list|,
specifier|final
name|boolean
name|isCompaction
parameter_list|)
block|{
specifier|final
name|HFileScanner
name|s
init|=
name|super
operator|.
name|getScanner
argument_list|(
name|cacheBlocks
argument_list|,
name|pread
argument_list|,
name|isCompaction
argument_list|)
decl_stmt|;
return|return
operator|new
name|HFileScanner
argument_list|()
block|{
specifier|final
name|HFileScanner
name|delegate
init|=
name|s
decl_stmt|;
specifier|public
name|boolean
name|atEnd
init|=
literal|false
decl_stmt|;
specifier|public
name|ByteBuffer
name|getKey
parameter_list|()
block|{
if|if
condition|(
name|atEnd
condition|)
return|return
literal|null
return|;
return|return
name|delegate
operator|.
name|getKey
argument_list|()
return|;
block|}
specifier|public
name|String
name|getKeyString
parameter_list|()
block|{
if|if
condition|(
name|atEnd
condition|)
return|return
literal|null
return|;
return|return
name|delegate
operator|.
name|getKeyString
argument_list|()
return|;
block|}
specifier|public
name|ByteBuffer
name|getValue
parameter_list|()
block|{
if|if
condition|(
name|atEnd
condition|)
return|return
literal|null
return|;
return|return
name|delegate
operator|.
name|getValue
argument_list|()
return|;
block|}
specifier|public
name|String
name|getValueString
parameter_list|()
block|{
if|if
condition|(
name|atEnd
condition|)
return|return
literal|null
return|;
return|return
name|delegate
operator|.
name|getValueString
argument_list|()
return|;
block|}
specifier|public
name|KeyValue
name|getKeyValue
parameter_list|()
block|{
if|if
condition|(
name|atEnd
condition|)
return|return
literal|null
return|;
return|return
name|delegate
operator|.
name|getKeyValue
argument_list|()
return|;
block|}
specifier|public
name|boolean
name|next
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|atEnd
condition|)
return|return
literal|false
return|;
name|boolean
name|b
init|=
name|delegate
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|b
condition|)
block|{
return|return
name|b
return|;
block|}
comment|// constrain the bottom.
if|if
condition|(
operator|!
name|top
condition|)
block|{
name|ByteBuffer
name|bb
init|=
name|getKey
argument_list|()
decl_stmt|;
if|if
condition|(
name|getComparator
argument_list|()
operator|.
name|compare
argument_list|(
name|bb
operator|.
name|array
argument_list|()
argument_list|,
name|bb
operator|.
name|arrayOffset
argument_list|()
argument_list|,
name|bb
operator|.
name|limit
argument_list|()
argument_list|,
name|splitkey
argument_list|,
literal|0
argument_list|,
name|splitkey
operator|.
name|length
argument_list|)
operator|>=
literal|0
condition|)
block|{
name|atEnd
operator|=
literal|true
expr_stmt|;
return|return
literal|false
return|;
block|}
block|}
return|return
literal|true
return|;
block|}
specifier|public
name|boolean
name|seekBefore
parameter_list|(
name|byte
index|[]
name|key
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|seekBefore
argument_list|(
name|key
argument_list|,
literal|0
argument_list|,
name|key
operator|.
name|length
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|seekBefore
parameter_list|(
name|byte
index|[]
name|key
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|top
condition|)
block|{
if|if
condition|(
name|getComparator
argument_list|()
operator|.
name|compare
argument_list|(
name|key
argument_list|,
name|offset
argument_list|,
name|length
argument_list|,
name|splitkey
argument_list|,
literal|0
argument_list|,
name|splitkey
operator|.
name|length
argument_list|)
operator|<
literal|0
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
else|else
block|{
comment|// The equals sign isn't strictly necessary just here to be consistent with seekTo
if|if
condition|(
name|getComparator
argument_list|()
operator|.
name|compare
argument_list|(
name|key
argument_list|,
name|offset
argument_list|,
name|length
argument_list|,
name|splitkey
argument_list|,
literal|0
argument_list|,
name|splitkey
operator|.
name|length
argument_list|)
operator|>=
literal|0
condition|)
block|{
return|return
name|this
operator|.
name|delegate
operator|.
name|seekBefore
argument_list|(
name|splitkey
argument_list|,
literal|0
argument_list|,
name|splitkey
operator|.
name|length
argument_list|)
return|;
block|}
block|}
return|return
name|this
operator|.
name|delegate
operator|.
name|seekBefore
argument_list|(
name|key
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|seekTo
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|top
condition|)
block|{
name|int
name|r
init|=
name|this
operator|.
name|delegate
operator|.
name|seekTo
argument_list|(
name|splitkey
argument_list|)
decl_stmt|;
if|if
condition|(
name|r
operator|<
literal|0
condition|)
block|{
comment|// midkey is< first key in file
return|return
name|this
operator|.
name|delegate
operator|.
name|seekTo
argument_list|()
return|;
block|}
if|if
condition|(
name|r
operator|>
literal|0
condition|)
block|{
return|return
name|this
operator|.
name|delegate
operator|.
name|next
argument_list|()
return|;
block|}
return|return
literal|true
return|;
block|}
name|boolean
name|b
init|=
name|delegate
operator|.
name|seekTo
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|b
condition|)
block|{
return|return
name|b
return|;
block|}
comment|// Check key.
name|ByteBuffer
name|k
init|=
name|this
operator|.
name|delegate
operator|.
name|getKey
argument_list|()
decl_stmt|;
return|return
name|this
operator|.
name|delegate
operator|.
name|getReader
argument_list|()
operator|.
name|getComparator
argument_list|()
operator|.
name|compare
argument_list|(
name|k
operator|.
name|array
argument_list|()
argument_list|,
name|k
operator|.
name|arrayOffset
argument_list|()
argument_list|,
name|k
operator|.
name|limit
argument_list|()
argument_list|,
name|splitkey
argument_list|,
literal|0
argument_list|,
name|splitkey
operator|.
name|length
argument_list|)
operator|<
literal|0
return|;
block|}
specifier|public
name|int
name|seekTo
parameter_list|(
name|byte
index|[]
name|key
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|seekTo
argument_list|(
name|key
argument_list|,
literal|0
argument_list|,
name|key
operator|.
name|length
argument_list|)
return|;
block|}
specifier|public
name|int
name|seekTo
parameter_list|(
name|byte
index|[]
name|key
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|top
condition|)
block|{
if|if
condition|(
name|getComparator
argument_list|()
operator|.
name|compare
argument_list|(
name|key
argument_list|,
name|offset
argument_list|,
name|length
argument_list|,
name|splitkey
argument_list|,
literal|0
argument_list|,
name|splitkey
operator|.
name|length
argument_list|)
operator|<
literal|0
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
block|}
else|else
block|{
if|if
condition|(
name|getComparator
argument_list|()
operator|.
name|compare
argument_list|(
name|key
argument_list|,
name|offset
argument_list|,
name|length
argument_list|,
name|splitkey
argument_list|,
literal|0
argument_list|,
name|splitkey
operator|.
name|length
argument_list|)
operator|>=
literal|0
condition|)
block|{
comment|// we would place the scanner in the second half.
comment|// it might be an error to return false here ever...
name|boolean
name|res
init|=
name|delegate
operator|.
name|seekBefore
argument_list|(
name|splitkey
argument_list|,
literal|0
argument_list|,
name|splitkey
operator|.
name|length
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|res
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Seeking for a key in bottom of file, but key exists in top of file, failed on seekBefore(midkey)"
argument_list|)
throw|;
block|}
return|return
literal|1
return|;
block|}
block|}
return|return
name|delegate
operator|.
name|seekTo
argument_list|(
name|key
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|reseekTo
parameter_list|(
name|byte
index|[]
name|key
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|reseekTo
argument_list|(
name|key
argument_list|,
literal|0
argument_list|,
name|key
operator|.
name|length
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|reseekTo
parameter_list|(
name|byte
index|[]
name|key
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
throws|throws
name|IOException
block|{
comment|//This function is identical to the corresponding seekTo function except
comment|//that we call reseekTo (and not seekTo) on the delegate.
if|if
condition|(
name|top
condition|)
block|{
if|if
condition|(
name|getComparator
argument_list|()
operator|.
name|compare
argument_list|(
name|key
argument_list|,
name|offset
argument_list|,
name|length
argument_list|,
name|splitkey
argument_list|,
literal|0
argument_list|,
name|splitkey
operator|.
name|length
argument_list|)
operator|<
literal|0
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
block|}
else|else
block|{
if|if
condition|(
name|getComparator
argument_list|()
operator|.
name|compare
argument_list|(
name|key
argument_list|,
name|offset
argument_list|,
name|length
argument_list|,
name|splitkey
argument_list|,
literal|0
argument_list|,
name|splitkey
operator|.
name|length
argument_list|)
operator|>=
literal|0
condition|)
block|{
comment|// we would place the scanner in the second half.
comment|// it might be an error to return false here ever...
name|boolean
name|res
init|=
name|delegate
operator|.
name|seekBefore
argument_list|(
name|splitkey
argument_list|,
literal|0
argument_list|,
name|splitkey
operator|.
name|length
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|res
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Seeking for a key in bottom of file, but"
operator|+
literal|" key exists in top of file, failed on seekBefore(midkey)"
argument_list|)
throw|;
block|}
return|return
literal|1
return|;
block|}
block|}
if|if
condition|(
name|atEnd
condition|)
block|{
comment|// skip the 'reseek' and just return 1.
return|return
literal|1
return|;
block|}
return|return
name|delegate
operator|.
name|reseekTo
argument_list|(
name|key
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
return|;
block|}
specifier|public
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
operator|.
name|Reader
name|getReader
parameter_list|()
block|{
return|return
name|this
operator|.
name|delegate
operator|.
name|getReader
argument_list|()
return|;
block|}
specifier|public
name|boolean
name|isSeeked
parameter_list|()
block|{
return|return
name|this
operator|.
name|delegate
operator|.
name|isSeeked
argument_list|()
return|;
block|}
block|}
return|;
block|}
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|getLastKey
parameter_list|()
block|{
if|if
condition|(
name|top
condition|)
block|{
return|return
name|super
operator|.
name|getLastKey
argument_list|()
return|;
block|}
comment|// Get a scanner that caches the block and that uses pread.
name|HFileScanner
name|scanner
init|=
name|getScanner
argument_list|(
literal|true
argument_list|,
literal|true
argument_list|)
decl_stmt|;
try|try
block|{
if|if
condition|(
name|scanner
operator|.
name|seekBefore
argument_list|(
name|this
operator|.
name|splitkey
argument_list|)
condition|)
block|{
return|return
name|Bytes
operator|.
name|toBytes
argument_list|(
name|scanner
operator|.
name|getKey
argument_list|()
argument_list|)
return|;
block|}
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
literal|"Failed seekBefore "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|this
operator|.
name|splitkey
argument_list|)
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|midkey
parameter_list|()
throws|throws
name|IOException
block|{
comment|// Returns null to indicate file is not splitable.
return|return
literal|null
return|;
block|}
block|}
end_class

end_unit

