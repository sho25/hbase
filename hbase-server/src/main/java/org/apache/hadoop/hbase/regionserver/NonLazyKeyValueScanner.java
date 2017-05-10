begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|commons
operator|.
name|lang
operator|.
name|NotImplementedException
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
name|Cell
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

begin_comment
comment|/**  * A "non-lazy" scanner which always does a real seek operation. Most scanners  * are inherited from this class.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|abstract
class|class
name|NonLazyKeyValueScanner
implements|implements
name|KeyValueScanner
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|requestSeek
parameter_list|(
name|Cell
name|kv
parameter_list|,
name|boolean
name|forward
parameter_list|,
name|boolean
name|useBloom
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|doRealSeek
argument_list|(
name|this
argument_list|,
name|kv
argument_list|,
name|forward
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|realSeekDone
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|enforceSeek
parameter_list|()
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|NotImplementedException
argument_list|(
literal|"enforceSeek must not be called on a "
operator|+
literal|"non-lazy scanner"
argument_list|)
throw|;
block|}
specifier|public
specifier|static
name|boolean
name|doRealSeek
parameter_list|(
name|KeyValueScanner
name|scanner
parameter_list|,
name|Cell
name|kv
parameter_list|,
name|boolean
name|forward
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|forward
condition|?
name|scanner
operator|.
name|reseek
argument_list|(
name|kv
argument_list|)
else|:
name|scanner
operator|.
name|seek
argument_list|(
name|kv
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|shouldUseScanner
parameter_list|(
name|Scan
name|scan
parameter_list|,
name|Store
name|store
parameter_list|,
name|long
name|oldestUnexpiredTS
parameter_list|)
block|{
comment|// No optimizations implemented by default.
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isFileScanner
parameter_list|()
block|{
comment|// Not a file by default.
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|Path
name|getFilePath
parameter_list|()
block|{
comment|// Not a file by default.
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|Cell
name|getNextIndexedKey
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|shipped
parameter_list|()
throws|throws
name|IOException
block|{
comment|// do nothing
block|}
block|}
end_class

end_unit

