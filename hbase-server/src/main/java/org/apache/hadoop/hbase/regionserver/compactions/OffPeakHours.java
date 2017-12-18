begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|compactions
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
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
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

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|abstract
class|class
name|OffPeakHours
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|OffPeakHours
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|OffPeakHours
name|DISABLED
init|=
operator|new
name|OffPeakHours
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|isOffPeakHour
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isOffPeakHour
parameter_list|(
name|int
name|targetHour
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
block|}
decl_stmt|;
specifier|public
specifier|static
name|OffPeakHours
name|getInstance
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|int
name|startHour
init|=
name|conf
operator|.
name|getInt
argument_list|(
name|CompactionConfiguration
operator|.
name|HBASE_HSTORE_OFFPEAK_START_HOUR
argument_list|,
operator|-
literal|1
argument_list|)
decl_stmt|;
name|int
name|endHour
init|=
name|conf
operator|.
name|getInt
argument_list|(
name|CompactionConfiguration
operator|.
name|HBASE_HSTORE_OFFPEAK_END_HOUR
argument_list|,
operator|-
literal|1
argument_list|)
decl_stmt|;
return|return
name|getInstance
argument_list|(
name|startHour
argument_list|,
name|endHour
argument_list|)
return|;
block|}
comment|/**    * @param startHour inclusive    * @param endHour exclusive    */
specifier|public
specifier|static
name|OffPeakHours
name|getInstance
parameter_list|(
name|int
name|startHour
parameter_list|,
name|int
name|endHour
parameter_list|)
block|{
if|if
condition|(
name|startHour
operator|==
operator|-
literal|1
operator|&&
name|endHour
operator|==
operator|-
literal|1
condition|)
block|{
return|return
name|DISABLED
return|;
block|}
if|if
condition|(
operator|!
name|isValidHour
argument_list|(
name|startHour
argument_list|)
operator|||
operator|!
name|isValidHour
argument_list|(
name|endHour
argument_list|)
condition|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isWarnEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Ignoring invalid start/end hour for peak hour : start = "
operator|+
name|startHour
operator|+
literal|" end = "
operator|+
name|endHour
operator|+
literal|". Valid numbers are [0-23]"
argument_list|)
expr_stmt|;
block|}
return|return
name|DISABLED
return|;
block|}
if|if
condition|(
name|startHour
operator|==
name|endHour
condition|)
block|{
return|return
name|DISABLED
return|;
block|}
return|return
operator|new
name|OffPeakHoursImpl
argument_list|(
name|startHour
argument_list|,
name|endHour
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|boolean
name|isValidHour
parameter_list|(
name|int
name|hour
parameter_list|)
block|{
return|return
literal|0
operator|<=
name|hour
operator|&&
name|hour
operator|<=
literal|23
return|;
block|}
comment|/**    * @return whether {@code targetHour} is off-peak hour    */
specifier|public
specifier|abstract
name|boolean
name|isOffPeakHour
parameter_list|(
name|int
name|targetHour
parameter_list|)
function_decl|;
comment|/**    * @return whether it is off-peak hour    */
specifier|public
specifier|abstract
name|boolean
name|isOffPeakHour
parameter_list|()
function_decl|;
specifier|private
specifier|static
class|class
name|OffPeakHoursImpl
extends|extends
name|OffPeakHours
block|{
specifier|final
name|int
name|startHour
decl_stmt|;
specifier|final
name|int
name|endHour
decl_stmt|;
comment|/**      * @param startHour inclusive      * @param endHour exclusive      */
name|OffPeakHoursImpl
parameter_list|(
name|int
name|startHour
parameter_list|,
name|int
name|endHour
parameter_list|)
block|{
name|this
operator|.
name|startHour
operator|=
name|startHour
expr_stmt|;
name|this
operator|.
name|endHour
operator|=
name|endHour
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isOffPeakHour
parameter_list|()
block|{
return|return
name|isOffPeakHour
argument_list|(
name|CurrentHourProvider
operator|.
name|getCurrentHour
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isOffPeakHour
parameter_list|(
name|int
name|targetHour
parameter_list|)
block|{
if|if
condition|(
name|startHour
operator|<=
name|endHour
condition|)
block|{
return|return
name|startHour
operator|<=
name|targetHour
operator|&&
name|targetHour
operator|<
name|endHour
return|;
block|}
return|return
name|targetHour
operator|<
name|endHour
operator|||
name|startHour
operator|<=
name|targetHour
return|;
block|}
block|}
block|}
end_class

end_unit

