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
name|master
operator|.
name|procedure
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

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|final
class|class
name|MasterProcedureConstants
block|{
specifier|private
name|MasterProcedureConstants
parameter_list|()
block|{}
comment|/** Number of threads used by the procedure executor */
specifier|public
specifier|static
specifier|final
name|String
name|MASTER_PROCEDURE_THREADS
init|=
literal|"hbase.master.procedure.threads"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|DEFAULT_MIN_MASTER_PROCEDURE_THREADS
init|=
literal|16
decl_stmt|;
comment|/**    * Procedure replay sanity check. In case a WAL is missing or unreadable we    * may lose information about pending/running procedures.    * Set this to true in case you want the Master failing on load if a corrupted    * procedure is encountred.    * (Default is off, because we prefer having the Master up and running and    * fix the "in transition" state "by hand")    */
specifier|public
specifier|static
specifier|final
name|String
name|EXECUTOR_ABORT_ON_CORRUPTION
init|=
literal|"hbase.procedure.abort.on.corruption"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|boolean
name|DEFAULT_EXECUTOR_ABORT_ON_CORRUPTION
init|=
literal|false
decl_stmt|;
block|}
end_class

end_unit

