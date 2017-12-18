begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**   * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|backup
package|;
end_package

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
name|log4j
operator|.
name|Level
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|log4j
operator|.
name|LogManager
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|log4j
operator|.
name|Logger
import|;
end_import

begin_comment
comment|/**  * Utility class for disabling Zk and client logging  *  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|final
class|class
name|LogUtils
block|{
specifier|private
name|LogUtils
parameter_list|()
block|{   }
comment|/**    * Disables Zk- and HBase client logging    */
specifier|static
name|void
name|disableZkAndClientLoggers
parameter_list|()
block|{
comment|// disable zookeeper log to avoid it mess up command output
name|Logger
name|zkLogger
init|=
name|LogManager
operator|.
name|getLogger
argument_list|(
literal|"org.apache.zookeeper"
argument_list|)
decl_stmt|;
name|zkLogger
operator|.
name|setLevel
argument_list|(
name|Level
operator|.
name|OFF
argument_list|)
expr_stmt|;
comment|// disable hbase zookeeper tool log to avoid it mess up command output
name|Logger
name|hbaseZkLogger
init|=
name|LogManager
operator|.
name|getLogger
argument_list|(
literal|"org.apache.hadoop.hbase.zookeeper"
argument_list|)
decl_stmt|;
name|hbaseZkLogger
operator|.
name|setLevel
argument_list|(
name|Level
operator|.
name|OFF
argument_list|)
expr_stmt|;
comment|// disable hbase client log to avoid it mess up command output
name|Logger
name|hbaseClientLogger
init|=
name|LogManager
operator|.
name|getLogger
argument_list|(
literal|"org.apache.hadoop.hbase.client"
argument_list|)
decl_stmt|;
name|hbaseClientLogger
operator|.
name|setLevel
argument_list|(
name|Level
operator|.
name|OFF
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

