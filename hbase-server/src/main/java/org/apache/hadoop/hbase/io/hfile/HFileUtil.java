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
name|io
operator|.
name|hfile
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
name|fs
operator|.
name|FSDataInputStream
import|;
end_import

begin_class
specifier|public
class|class
name|HFileUtil
block|{
comment|/** guards against NullPointer    * utility which tries to seek on the DFSIS and will try an alternative source    * if the FSDataInputStream throws an NPE HBASE-17501    * @param istream    * @param offset    * @throws IOException    */
specifier|static
specifier|public
name|void
name|seekOnMultipleSources
parameter_list|(
name|FSDataInputStream
name|istream
parameter_list|,
name|long
name|offset
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
comment|// attempt to seek inside of current blockReader
name|istream
operator|.
name|seek
argument_list|(
name|offset
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NullPointerException
name|e
parameter_list|)
block|{
comment|// retry the seek on an alternate copy of the data
comment|// this can occur if the blockReader on the DFSInputStream is null
name|istream
operator|.
name|seekToNewSource
argument_list|(
name|offset
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

