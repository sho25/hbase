begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|io
operator|.
name|hfile
operator|.
name|HFile
operator|.
name|Reader
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

begin_comment
comment|/**  * A KeyValue scanner that iterates over a single HFile  */
end_comment

begin_class
class|class
name|StoreFileScanner
implements|implements
name|KeyValueScanner
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
name|Store
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|HFileScanner
name|hfs
decl_stmt|;
specifier|private
name|KeyValue
name|cur
init|=
literal|null
decl_stmt|;
comment|/**    * Implements a {@link KeyValueScanner} on top of the specified {@link HFileScanner}    * @param hfs HFile scanner    */
specifier|private
name|StoreFileScanner
parameter_list|(
name|HFileScanner
name|hfs
parameter_list|)
block|{
name|this
operator|.
name|hfs
operator|=
name|hfs
expr_stmt|;
block|}
comment|/**    * Return an array of scanners corresponding to the given    * set of store files.    */
specifier|public
specifier|static
name|List
argument_list|<
name|StoreFileScanner
argument_list|>
name|getScannersForStoreFiles
parameter_list|(
name|Collection
argument_list|<
name|StoreFile
argument_list|>
name|filesToCompact
parameter_list|,
name|boolean
name|cacheBlocks
parameter_list|,
name|boolean
name|usePread
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|StoreFileScanner
argument_list|>
name|scanners
init|=
operator|new
name|ArrayList
argument_list|<
name|StoreFileScanner
argument_list|>
argument_list|(
name|filesToCompact
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|StoreFile
name|file
range|:
name|filesToCompact
control|)
block|{
name|Reader
name|r
init|=
name|file
operator|.
name|createReader
argument_list|()
decl_stmt|;
name|scanners
operator|.
name|add
argument_list|(
operator|new
name|StoreFileScanner
argument_list|(
name|r
operator|.
name|getScanner
argument_list|(
name|cacheBlocks
argument_list|,
name|usePread
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|scanners
return|;
block|}
specifier|public
name|HFileScanner
name|getHFileScanner
parameter_list|()
block|{
return|return
name|this
operator|.
name|hfs
return|;
block|}
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"StoreFileScanner["
operator|+
name|hfs
operator|.
name|toString
argument_list|()
operator|+
literal|", cur="
operator|+
name|cur
operator|+
literal|"]"
return|;
block|}
specifier|public
name|KeyValue
name|peek
parameter_list|()
block|{
return|return
name|cur
return|;
block|}
specifier|public
name|KeyValue
name|next
parameter_list|()
throws|throws
name|IOException
block|{
name|KeyValue
name|retKey
init|=
name|cur
decl_stmt|;
name|cur
operator|=
name|hfs
operator|.
name|getKeyValue
argument_list|()
expr_stmt|;
try|try
block|{
comment|// only seek if we arent at the end. cur == null implies 'end'.
if|if
condition|(
name|cur
operator|!=
literal|null
condition|)
name|hfs
operator|.
name|next
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Could not iterate "
operator|+
name|this
argument_list|,
name|e
argument_list|)
throw|;
block|}
return|return
name|retKey
return|;
block|}
specifier|public
name|boolean
name|seek
parameter_list|(
name|KeyValue
name|key
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
if|if
condition|(
operator|!
name|seekAtOrAfter
argument_list|(
name|hfs
argument_list|,
name|key
argument_list|)
condition|)
block|{
name|close
argument_list|()
expr_stmt|;
return|return
literal|false
return|;
block|}
name|cur
operator|=
name|hfs
operator|.
name|getKeyValue
argument_list|()
expr_stmt|;
name|hfs
operator|.
name|next
argument_list|()
expr_stmt|;
return|return
literal|true
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Could not seek "
operator|+
name|this
argument_list|,
name|ioe
argument_list|)
throw|;
block|}
block|}
specifier|public
name|void
name|close
parameter_list|()
block|{
comment|// Nothing to close on HFileScanner?
name|cur
operator|=
literal|null
expr_stmt|;
block|}
comment|/**    *    * @param s    * @param k    * @return    * @throws IOException    */
specifier|public
specifier|static
name|boolean
name|seekAtOrAfter
parameter_list|(
name|HFileScanner
name|s
parameter_list|,
name|KeyValue
name|k
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|result
init|=
name|s
operator|.
name|seekTo
argument_list|(
name|k
operator|.
name|getBuffer
argument_list|()
argument_list|,
name|k
operator|.
name|getKeyOffset
argument_list|()
argument_list|,
name|k
operator|.
name|getKeyLength
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|result
operator|<
literal|0
condition|)
block|{
comment|// Passed KV is smaller than first KV in file, work from start of file
return|return
name|s
operator|.
name|seekTo
argument_list|()
return|;
block|}
elseif|else
if|if
condition|(
name|result
operator|>
literal|0
condition|)
block|{
comment|// Passed KV is larger than current KV in file, if there is a next
comment|// it is the "after", if not then this scanner is done.
return|return
name|s
operator|.
name|next
argument_list|()
return|;
block|}
comment|// Seeked to the exact key
return|return
literal|true
return|;
block|}
block|}
end_class

end_unit

