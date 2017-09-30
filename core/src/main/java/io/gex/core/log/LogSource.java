package io.gex.core.log;


public enum LogSource {
    cli(1),
    gexd(2),
    server(3);

    private Integer number;

    LogSource(Integer number) {
        this.number = number;
    }

    public Integer getNumber() {
        return number;
    }
}
