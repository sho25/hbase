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

begin_comment
comment|// TODO move this to hbase-annotations non-test-jar
end_comment

begin_comment
comment|/**  * This class defines constants for different classes of hbase limited private apis  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
specifier|final
class|class
name|HBaseInterfaceAudience
block|{
comment|/**    * Can't create this class.    */
specifier|private
name|HBaseInterfaceAudience
parameter_list|()
block|{}
specifier|public
specifier|static
specifier|final
name|String
name|COPROC
init|=
literal|"Coprocesssor"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|REPLICATION
init|=
literal|"Replication"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|PHOENIX
init|=
literal|"Phoenix"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|SPARK
init|=
literal|"Spark"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|UNITTEST
init|=
literal|"Unittest"
decl_stmt|;
comment|/**    * Denotes class names that appear in user facing configuration files.    */
specifier|public
specifier|static
specifier|final
name|String
name|CONFIG
init|=
literal|"Configuration"
decl_stmt|;
comment|/**    * Denotes classes used as tools (Used from cmd line). Usually, the compatibility is required    * for class name, and arguments.    */
specifier|public
specifier|static
specifier|final
name|String
name|TOOLS
init|=
literal|"Tools"
decl_stmt|;
comment|/**    * Denotes classes used by hbck tool for fixing inconsistent state of HBase.    */
specifier|public
specifier|static
specifier|final
name|String
name|HBCK
init|=
literal|"HBCK"
decl_stmt|;
block|}
end_class

end_unit

