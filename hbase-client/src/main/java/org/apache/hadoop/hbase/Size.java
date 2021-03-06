begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright The Apache Software Foundation  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  * http://www.apache.org/licenses/LICENSE-2.0  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|java
operator|.
name|math
operator|.
name|BigDecimal
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Objects
import|;
end_import

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
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Preconditions
import|;
end_import

begin_comment
comment|/**  * It is used to represent the size with different units.  * This class doesn't serve for the precise computation.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
specifier|final
class|class
name|Size
implements|implements
name|Comparable
argument_list|<
name|Size
argument_list|>
block|{
specifier|public
specifier|static
specifier|final
name|Size
name|ZERO
init|=
operator|new
name|Size
argument_list|(
literal|0
argument_list|,
name|Unit
operator|.
name|KILOBYTE
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|BigDecimal
name|SCALE_BASE
init|=
name|BigDecimal
operator|.
name|valueOf
argument_list|(
literal|1024D
argument_list|)
decl_stmt|;
specifier|public
enum|enum
name|Unit
block|{
comment|// keep the room to add more units for HBase 10.x
name|PETABYTE
argument_list|(
literal|100
argument_list|,
literal|"PB"
argument_list|)
block|,
name|TERABYTE
argument_list|(
literal|99
argument_list|,
literal|"TB"
argument_list|)
block|,
name|GIGABYTE
argument_list|(
literal|98
argument_list|,
literal|"GB"
argument_list|)
block|,
name|MEGABYTE
argument_list|(
literal|97
argument_list|,
literal|"MB"
argument_list|)
block|,
name|KILOBYTE
argument_list|(
literal|96
argument_list|,
literal|"KB"
argument_list|)
block|,
name|BYTE
argument_list|(
literal|95
argument_list|,
literal|"B"
argument_list|)
block|;
specifier|private
specifier|final
name|int
name|orderOfSize
decl_stmt|;
specifier|private
specifier|final
name|String
name|simpleName
decl_stmt|;
name|Unit
parameter_list|(
name|int
name|orderOfSize
parameter_list|,
name|String
name|simpleName
parameter_list|)
block|{
name|this
operator|.
name|orderOfSize
operator|=
name|orderOfSize
expr_stmt|;
name|this
operator|.
name|simpleName
operator|=
name|simpleName
expr_stmt|;
block|}
specifier|public
name|int
name|getOrderOfSize
parameter_list|()
block|{
return|return
name|orderOfSize
return|;
block|}
specifier|public
name|String
name|getSimpleName
parameter_list|()
block|{
return|return
name|simpleName
return|;
block|}
block|}
specifier|private
specifier|final
name|double
name|value
decl_stmt|;
specifier|private
specifier|final
name|Unit
name|unit
decl_stmt|;
specifier|public
name|Size
parameter_list|(
name|double
name|value
parameter_list|,
name|Unit
name|unit
parameter_list|)
block|{
if|if
condition|(
name|value
operator|<
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"The value:"
operator|+
name|value
operator|+
literal|" can't be negative"
argument_list|)
throw|;
block|}
name|this
operator|.
name|value
operator|=
name|value
expr_stmt|;
name|this
operator|.
name|unit
operator|=
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|unit
argument_list|)
expr_stmt|;
block|}
comment|/**    * @return size unit    */
specifier|public
name|Unit
name|getUnit
parameter_list|()
block|{
return|return
name|unit
return|;
block|}
comment|/**    * get the value    */
specifier|public
name|long
name|getLongValue
parameter_list|()
block|{
return|return
operator|(
name|long
operator|)
name|value
return|;
block|}
comment|/**    * get the value    */
specifier|public
name|double
name|get
parameter_list|()
block|{
return|return
name|value
return|;
block|}
comment|/**    * get the value which is converted to specified unit.    *    * @param unit size unit    * @return the converted value    */
specifier|public
name|double
name|get
parameter_list|(
name|Unit
name|unit
parameter_list|)
block|{
if|if
condition|(
name|value
operator|==
literal|0
condition|)
block|{
return|return
name|value
return|;
block|}
name|int
name|diff
init|=
name|this
operator|.
name|unit
operator|.
name|getOrderOfSize
argument_list|()
operator|-
name|unit
operator|.
name|getOrderOfSize
argument_list|()
decl_stmt|;
if|if
condition|(
name|diff
operator|==
literal|0
condition|)
block|{
return|return
name|value
return|;
block|}
name|BigDecimal
name|rval
init|=
name|BigDecimal
operator|.
name|valueOf
argument_list|(
name|value
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|!=
name|Math
operator|.
name|abs
argument_list|(
name|diff
argument_list|)
condition|;
operator|++
name|i
control|)
block|{
name|rval
operator|=
name|diff
operator|>
literal|0
condition|?
name|rval
operator|.
name|multiply
argument_list|(
name|SCALE_BASE
argument_list|)
else|:
name|rval
operator|.
name|divide
argument_list|(
name|SCALE_BASE
argument_list|)
expr_stmt|;
block|}
return|return
name|rval
operator|.
name|doubleValue
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|compareTo
parameter_list|(
name|Size
name|other
parameter_list|)
block|{
name|int
name|diff
init|=
name|unit
operator|.
name|getOrderOfSize
argument_list|()
operator|-
name|other
operator|.
name|unit
operator|.
name|getOrderOfSize
argument_list|()
decl_stmt|;
if|if
condition|(
name|diff
operator|==
literal|0
condition|)
block|{
return|return
name|Double
operator|.
name|compare
argument_list|(
name|value
argument_list|,
name|other
operator|.
name|value
argument_list|)
return|;
block|}
name|BigDecimal
name|thisValue
init|=
name|BigDecimal
operator|.
name|valueOf
argument_list|(
name|value
argument_list|)
decl_stmt|;
name|BigDecimal
name|otherValue
init|=
name|BigDecimal
operator|.
name|valueOf
argument_list|(
name|other
operator|.
name|value
argument_list|)
decl_stmt|;
if|if
condition|(
name|diff
operator|>
literal|0
condition|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|!=
name|Math
operator|.
name|abs
argument_list|(
name|diff
argument_list|)
condition|;
operator|++
name|i
control|)
block|{
name|thisValue
operator|=
name|thisValue
operator|.
name|multiply
argument_list|(
name|SCALE_BASE
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|!=
name|Math
operator|.
name|abs
argument_list|(
name|diff
argument_list|)
condition|;
operator|++
name|i
control|)
block|{
name|otherValue
operator|=
name|otherValue
operator|.
name|multiply
argument_list|(
name|SCALE_BASE
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|thisValue
operator|.
name|compareTo
argument_list|(
name|otherValue
argument_list|)
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
name|value
operator|+
name|unit
operator|.
name|getSimpleName
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
if|if
condition|(
name|obj
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|obj
operator|==
name|this
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
name|obj
operator|instanceof
name|Size
condition|)
block|{
return|return
name|compareTo
argument_list|(
operator|(
name|Size
operator|)
name|obj
argument_list|)
operator|==
literal|0
return|;
block|}
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|Objects
operator|.
name|hash
argument_list|(
name|value
argument_list|,
name|unit
argument_list|)
return|;
block|}
block|}
end_class

end_unit

