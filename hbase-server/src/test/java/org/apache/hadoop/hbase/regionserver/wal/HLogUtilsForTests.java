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
operator|.
name|wal
package|;
end_package

begin_comment
comment|/**  * An Utility testcase that returns the number of log files that  * were rolled to be accessed from outside packages.  *   * This class makes available methods that are package protected.  *  This is interesting for test only.  */
end_comment

begin_class
specifier|public
class|class
name|HLogUtilsForTests
block|{
comment|/**    *     * @param log    * @return    */
specifier|public
specifier|static
name|int
name|getNumRolledLogFiles
parameter_list|(
name|HLog
name|log
parameter_list|)
block|{
return|return
operator|(
operator|(
name|FSHLog
operator|)
name|log
operator|)
operator|.
name|getNumRolledLogFiles
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|int
name|getNumEntries
parameter_list|(
name|HLog
name|log
parameter_list|)
block|{
return|return
operator|(
operator|(
name|FSHLog
operator|)
name|log
operator|)
operator|.
name|getNumEntries
argument_list|()
return|;
block|}
block|}
end_class

end_unit

