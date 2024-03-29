begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements. See the NOTICE file distributed with this  * work for additional information regarding copyright ownership. The ASF  * licenses this file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the  * License for the specific language governing permissions and limitations under  * the License.  */
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

begin_comment
comment|/**  * A nano-second timer.  *<p>  * Copied from  *<a href="https://issues.apache.org/jira/browse/HADOOP-3315">hadoop-3315 tfile</a>.  * Remove after tfile is committed and use the tfile version of this class  * instead.</p>  */
end_comment

begin_class
specifier|public
class|class
name|NanoTimer
block|{
specifier|private
name|long
name|last
init|=
operator|-
literal|1
decl_stmt|;
specifier|private
name|boolean
name|started
init|=
literal|false
decl_stmt|;
specifier|private
name|long
name|cumulate
init|=
literal|0
decl_stmt|;
comment|/**    * Constructor    *    * @param start    *          Start the timer upon construction.    */
specifier|public
name|NanoTimer
parameter_list|(
name|boolean
name|start
parameter_list|)
block|{
if|if
condition|(
name|start
condition|)
name|this
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
comment|/**    * Start the timer.    *    * Note: No effect if timer is already started.    */
specifier|public
name|void
name|start
parameter_list|()
block|{
if|if
condition|(
operator|!
name|this
operator|.
name|started
condition|)
block|{
name|this
operator|.
name|last
operator|=
name|System
operator|.
name|nanoTime
argument_list|()
expr_stmt|;
name|this
operator|.
name|started
operator|=
literal|true
expr_stmt|;
block|}
block|}
comment|/**    * Stop the timer.    *    * Note: No effect if timer is already stopped.    */
specifier|public
name|void
name|stop
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|started
condition|)
block|{
name|this
operator|.
name|started
operator|=
literal|false
expr_stmt|;
name|this
operator|.
name|cumulate
operator|+=
name|System
operator|.
name|nanoTime
argument_list|()
operator|-
name|this
operator|.
name|last
expr_stmt|;
block|}
block|}
comment|/**    * Read the timer.    *    * @return the elapsed time in nano-seconds. Note: If the timer is never    *         started before, -1 is returned.    */
specifier|public
name|long
name|read
parameter_list|()
block|{
if|if
condition|(
operator|!
name|readable
argument_list|()
condition|)
return|return
operator|-
literal|1
return|;
return|return
name|this
operator|.
name|cumulate
return|;
block|}
comment|/**    * Reset the timer.    */
specifier|public
name|void
name|reset
parameter_list|()
block|{
name|this
operator|.
name|last
operator|=
operator|-
literal|1
expr_stmt|;
name|this
operator|.
name|started
operator|=
literal|false
expr_stmt|;
name|this
operator|.
name|cumulate
operator|=
literal|0
expr_stmt|;
block|}
comment|/**    * Checking whether the timer is started    *    * @return true if timer is started.    */
specifier|public
name|boolean
name|isStarted
parameter_list|()
block|{
return|return
name|this
operator|.
name|started
return|;
block|}
comment|/**    * Format the elapsed time to a human understandable string.    *    * Note: If timer is never started, "ERR" will be returned.    */
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
if|if
condition|(
operator|!
name|readable
argument_list|()
condition|)
block|{
return|return
literal|"ERR"
return|;
block|}
return|return
name|NanoTimer
operator|.
name|nanoTimeToString
argument_list|(
name|this
operator|.
name|cumulate
argument_list|)
return|;
block|}
comment|/**    * A utility method to format a time duration in nano seconds into a human    * understandable stirng.    *    * @param t    *          Time duration in nano seconds.    * @return String representation.    */
specifier|public
specifier|static
name|String
name|nanoTimeToString
parameter_list|(
name|long
name|t
parameter_list|)
block|{
if|if
condition|(
name|t
operator|<
literal|0
condition|)
return|return
literal|"ERR"
return|;
if|if
condition|(
name|t
operator|==
literal|0
condition|)
return|return
literal|"0"
return|;
if|if
condition|(
name|t
operator|<
literal|1000
condition|)
block|{
return|return
name|t
operator|+
literal|"ns"
return|;
block|}
name|double
name|us
init|=
operator|(
name|double
operator|)
name|t
operator|/
literal|1000
decl_stmt|;
if|if
condition|(
name|us
operator|<
literal|1000
condition|)
block|{
return|return
name|String
operator|.
name|format
argument_list|(
literal|"%.2fus"
argument_list|,
name|us
argument_list|)
return|;
block|}
name|double
name|ms
init|=
name|us
operator|/
literal|1000
decl_stmt|;
if|if
condition|(
name|ms
operator|<
literal|1000
condition|)
block|{
return|return
name|String
operator|.
name|format
argument_list|(
literal|"%.2fms"
argument_list|,
name|ms
argument_list|)
return|;
block|}
name|double
name|ss
init|=
name|ms
operator|/
literal|1000
decl_stmt|;
if|if
condition|(
name|ss
operator|<
literal|1000
condition|)
block|{
return|return
name|String
operator|.
name|format
argument_list|(
literal|"%.2fs"
argument_list|,
name|ss
argument_list|)
return|;
block|}
name|long
name|mm
init|=
operator|(
name|long
operator|)
name|ss
operator|/
literal|60
decl_stmt|;
name|ss
operator|-=
name|mm
operator|*
literal|60
expr_stmt|;
name|long
name|hh
init|=
name|mm
operator|/
literal|60
decl_stmt|;
name|mm
operator|-=
name|hh
operator|*
literal|60
expr_stmt|;
name|long
name|dd
init|=
name|hh
operator|/
literal|24
decl_stmt|;
name|hh
operator|-=
name|dd
operator|*
literal|24
expr_stmt|;
if|if
condition|(
name|dd
operator|>
literal|0
condition|)
block|{
return|return
name|String
operator|.
name|format
argument_list|(
literal|"%dd%dh"
argument_list|,
name|dd
argument_list|,
name|hh
argument_list|)
return|;
block|}
if|if
condition|(
name|hh
operator|>
literal|0
condition|)
block|{
return|return
name|String
operator|.
name|format
argument_list|(
literal|"%dh%dm"
argument_list|,
name|hh
argument_list|,
name|mm
argument_list|)
return|;
block|}
if|if
condition|(
name|mm
operator|>
literal|0
condition|)
block|{
return|return
name|String
operator|.
name|format
argument_list|(
literal|"%dm%.1fs"
argument_list|,
name|mm
argument_list|,
name|ss
argument_list|)
return|;
block|}
return|return
name|String
operator|.
name|format
argument_list|(
literal|"%.2fs"
argument_list|,
name|ss
argument_list|)
return|;
comment|/**      * StringBuilder sb = new StringBuilder(); String sep = "";      *      * if (dd> 0) { String unit = (dd> 1) ? "days" : "day";      * sb.append(String.format("%s%d%s", sep, dd, unit)); sep = " "; }      *      * if (hh> 0) { String unit = (hh> 1) ? "hrs" : "hr";      * sb.append(String.format("%s%d%s", sep, hh, unit)); sep = " "; }      *      * if (mm> 0) { String unit = (mm> 1) ? "mins" : "min";      * sb.append(String.format("%s%d%s", sep, mm, unit)); sep = " "; }      *      * if (ss> 0) { String unit = (ss> 1) ? "secs" : "sec";      * sb.append(String.format("%s%.3f%s", sep, ss, unit)); sep = " "; }      *      * return sb.toString();      */
block|}
specifier|private
name|boolean
name|readable
parameter_list|()
block|{
return|return
name|this
operator|.
name|last
operator|!=
operator|-
literal|1
return|;
block|}
comment|/**    * Simple tester.    *    * @param args    */
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
block|{
name|long
name|i
init|=
literal|7
decl_stmt|;
for|for
control|(
name|int
name|x
init|=
literal|0
init|;
name|x
operator|<
literal|20
condition|;
operator|++
name|x
operator|,
name|i
operator|*=
literal|7
control|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|NanoTimer
operator|.
name|nanoTimeToString
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

